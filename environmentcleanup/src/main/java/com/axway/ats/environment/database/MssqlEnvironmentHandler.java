/*
 * Copyright 2017 Axway Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axway.ats.environment.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.axway.ats.common.systemproperties.AtsSystemProperties;
import com.axway.ats.core.dbaccess.ColumnDescription;
import com.axway.ats.core.dbaccess.ConnectionPool;
import com.axway.ats.core.dbaccess.DbRecordValue;
import com.axway.ats.core.dbaccess.DbRecordValuesList;
import com.axway.ats.core.dbaccess.DbUtils;
import com.axway.ats.core.dbaccess.MssqlColumnDescription;
import com.axway.ats.core.dbaccess.exceptions.DbException;
import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.core.dbaccess.mssql.MssqlDbProvider;
import com.axway.ats.core.utils.IoUtils;
import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.environment.database.exceptions.ColumnHasNoDefaultValueException;
import com.axway.ats.environment.database.exceptions.DatabaseEnvironmentCleanupException;
import com.axway.ats.environment.database.model.DbTable;

class MssqlEnvironmentHandler extends AbstractEnvironmentHandler {

    private static final Logger LOG            = Logger.getLogger(MssqlEnvironmentHandler.class);
    private static final String HEX_PREFIX_STR = "0x";

    MssqlEnvironmentHandler( DbConnSQLServer dbConnection,
                             MssqlDbProvider dbProvider ) {

        super(dbConnection, dbProvider);
    }

    @Override
    protected List<ColumnDescription> getColumnsToSelect(
                                                          DbTable table,
                                                          String userName ) throws DbException,
                                                                            ColumnHasNoDefaultValueException {

        String selectColumnsInfo = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, "
                                   + "columnproperty(object_id('"
                                   + table.getTableName()
                                   + "'), COLUMN_NAME,'IsIdentity') as isIdentity "
                                   + "FROM information_schema.COLUMNS WHERE table_name LIKE '"
                                   + table.getTableName() + "'";
        ArrayList<ColumnDescription> columnsToSelect = new ArrayList<ColumnDescription>();
        DbRecordValuesList[] columnsMetaData = null;
        try {
            columnsMetaData = this.dbProvider.select(selectColumnsInfo);
        } catch (DbException e) {
            throw new DbException("Could not get columns for table " + table.getTableName()
                                  + ". Check if the table is existing and that the user has permissions. See more details in the trace.",
                                  e);
        }

        table.setIdentityColumnPresent(false); // the Identity column can be skipped(excluded)
        for (DbRecordValuesList columnMetaData : columnsMetaData) {

            String columnName = (String) columnMetaData.get("COLUMN_NAME");

            //check if the column should be skipped in the backup
            if (!table.getColumnsToExclude().contains(columnName)) {

                ColumnDescription colDescription = new MssqlColumnDescription(columnName,
                                                                              (String) columnMetaData.get("DATA_TYPE"));
                columnsToSelect.add(colDescription);
                if (columnMetaData.get("isIdentity") != null && (Integer) columnMetaData.get("isIdentity") == 1) {
                    table.setIdentityColumnPresent(true);
                }
            } else {
                //if this column has no default value, we cannot skip it in the backup
                if (columnMetaData.get("COLUMN_DEFAULT") == null) {
                    LOG.error("Cannot skip columns with no default values while creating backup");
                    throw new ColumnHasNoDefaultValueException(table.getTableName(), columnName);
                }
            }
        }

        return columnsToSelect;
    }

    @Override
    protected void writeTableToFile(
                                     List<ColumnDescription> columns,
                                     DbTable table,
                                     DbRecordValuesList[] records,
                                     Writer fileWriter ) throws IOException, ParseException {

        if (this.addLocks) {
            fileWriter.write("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE " + EOL_MARKER
                             + AtsSystemProperties.SYSTEM_LINE_SEPARATOR);
        }

        if (this.dropEntireTable) {
            fileWriter.write(DROP_TABLE_MARKER + table.getTableSchema() + "." + table.getTableName()
                             + AtsSystemProperties.SYSTEM_LINE_SEPARATOR);
        } else if (!this.deleteStatementsInserted) {
            writeDeleteStatements(fileWriter);
        }

        if (table.getAutoIncrementResetValue() != null) {
            fileWriter.write("DBCC CHECKIDENT ('" + table.getTableName() + "', RESEED, "
                             + table.getAutoIncrementResetValue() + ");" + EOL_MARKER
                             + AtsSystemProperties.SYSTEM_LINE_SEPARATOR);
        }

        if (records.length > 0) {

            StringBuilder insertStatement = new StringBuilder();
            String insertBegin = "INSERT INTO " + table.getTableName() + "(" + getColumnsString(columns)
                                 + ") VALUES (";
            String insertEnd = null;
            if (table.isIdentityColumnPresent()) {
                insertBegin = "SET IDENTITY_INSERT " + table.getTableName() + " ON; " + insertBegin;
                insertEnd = "); SET IDENTITY_INSERT " + table.getTableName() + " OFF;" + EOL_MARKER
                            + AtsSystemProperties.SYSTEM_LINE_SEPARATOR;
            } else {
                insertEnd = ");" + EOL_MARKER + AtsSystemProperties.SYSTEM_LINE_SEPARATOR;
            }

            for (DbRecordValuesList record : records) {

                insertStatement.append(insertBegin);

                for (int i = 0; i < record.size(); i++) {

                    DbRecordValue recordValue = record.get(i);
                    String fieldValue = (String) recordValue.getValue();

                    // extract specific values depending on their type
                    insertStatement.append(extractValue(columns.get(i), fieldValue));
                    insertStatement.append(",");
                }
                //remove the last comma
                insertStatement.delete(insertStatement.length() - 1, insertStatement.length());
                insertStatement.append(insertEnd);
                fileWriter.write(insertStatement.toString()); // limit memory allocation for big tables. Write after each row
                insertStatement.setLength(0);
            }
            fileWriter.flush();
        }
    }

    protected String getColumnsString(
                                       List<ColumnDescription> columns ) {

        StringBuilder columnsBuilder = new StringBuilder();

        //create the columns string
        for (ColumnDescription column : columns) {
            columnsBuilder.append('[' + column.getName());
            columnsBuilder.append("],");

        }
        //remove the last comma
        if (columnsBuilder.length() > 1) {
            columnsBuilder.delete(columnsBuilder.length() - 1, columnsBuilder.length());
        }

        return columnsBuilder.toString();
    }

    @Override
    protected String disableForeignKeyChecksStart() {

        // Disable all constraints
        return "EXEC sp_msforeachtable \"ALTER TABLE ? NOCHECK CONSTRAINT all\";" + EOL_MARKER
               + AtsSystemProperties.SYSTEM_LINE_SEPARATOR;
    }

    @Override
    protected String disableForeignKeyChecksEnd() {

        // Enable all constraints
        return "EXEC sp_msforeachtable @command1=\"ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all\";"
               + EOL_MARKER + AtsSystemProperties.SYSTEM_LINE_SEPARATOR;
    }

    @Override
    protected void writeDeleteStatements( Writer fileWriter ) throws IOException {

        if (this.includeDeleteStatements) {
            for (Entry<String, DbTable> entry : dbTables.entrySet()) {
                DbTable dbTable = entry.getValue();
                String deleteQuery = "DELETE FROM " + dbTable.getTableName();
                fileWriter.write(deleteQuery + ";" + EOL_MARKER
                                 + AtsSystemProperties.SYSTEM_LINE_SEPARATOR);
            }
            this.deleteStatementsInserted = true;
        }

    }

    // extracts the specific value, considering it's type and the specifics associated with it
    private StringBuilder extractValue(
                                        ColumnDescription column,
                                        String fieldValue ) throws ParseException {

        if (fieldValue == null) {
            return new StringBuilder("NULL");
        }

        StringBuilder insertStatement = new StringBuilder();
        // non-string values. Should not be in quotes and do not need escaping
        if (column.isTypeNumeric()) {

            // BIT type stores only two types of values - 0 and 1, we need to
            // extract them and pass them back as string
            if (column.isTypeBit()) {
                // The value must be a hex number 0xnnnn
                if (fieldValue.startsWith(HEX_PREFIX_STR)) {
                    // value already in hex notation. This is because for BIT(>1) resultSet.getObject(col) currently
                    // returns byte[]
                    insertStatement.append(fieldValue);
                } else {
                    insertStatement.append(HEX_PREFIX_STR + fieldValue);
                }
            } else {
                insertStatement.append(fieldValue);
            }
        } else if (column.isTypeBinary()) {

            if (fieldValue.startsWith(HEX_PREFIX_STR)) {
                insertStatement.append(fieldValue);
            } else {
                insertStatement.append(HEX_PREFIX_STR + fieldValue);
            }
        } else {

            insertStatement.append('\'');
            insertStatement.append(fieldValue.replace("'", "''"));
            insertStatement.append('\'');
        }

        return insertStatement;
    }

    /**
     * @see com.axway.ats.environment.database.model.RestoreHandler#restore(java.lang.String)
     */
    public void restore(
                         String backupFileName ) throws DatabaseEnvironmentCleanupException {

        BufferedReader backupReader = null;
        Connection connection = null;

        //we need to preserve the auto commit option, as the connections are pooled
        boolean isAutoCommit = true;

        try {
            LOG.info("Started restore of database backup from file '" + backupFileName + "'");

            backupReader = new BufferedReader(new FileReader(new File(backupFileName)));

            connection = ConnectionPool.getConnection(dbConnection);

            isAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            String line = backupReader.readLine();
            while (line != null) {

                sql.append(line);

                if (line.startsWith(DROP_TABLE_MARKER)) {

                    String tableName = line.substring(DROP_TABLE_MARKER.length()).trim();
                    dropAndRecreateTable(connection, tableName);

                } else if (line.endsWith(EOL_MARKER)) {

                    // remove the EOL marker
                    sql.delete(sql.length() - EOL_MARKER.length(), sql.length());
                    PreparedStatement updateStatement = connection.prepareStatement(sql.toString());

                    // catch the exception and rollback, otherwise we are locked
                    try {
                        updateStatement.execute();
                    } catch (SQLException sqle) {
                        //we have to roll back the transaction and re-throw the exception
                        connection.rollback();
                        throw new SQLException("Error executing restore satement: " + sql.toString(), sqle);
                    } finally {
                        DbUtils.closeStatement(updateStatement);
                    }
                    sql = new StringBuilder();
                } else {
                    //add a new line
                    //FIXME: this code will add the system line ending - it
                    //is not guaranteed that this was the actual line ending
                    sql.append(AtsSystemProperties.SYSTEM_LINE_SEPARATOR);
                }

                line = backupReader.readLine();
            }

            try {
                //commit the transaction
                connection.commit();

            } catch (SQLException sqle) {
                //we have to roll back the transaction and re-throw the exception
                connection.rollback();
                throw sqle;
            }

            LOG.info("Completed restore of database backup from file '" + backupFileName + "'");
        } catch (IOException ioe) {
            throw new DatabaseEnvironmentCleanupException(ERROR_RESTORING_BACKUP + backupFileName, ioe);
        } catch (SQLException sqle) {
            throw new DatabaseEnvironmentCleanupException(ERROR_RESTORING_BACKUP + backupFileName, sqle);
        } catch (DbException dbe) {
            throw new DatabaseEnvironmentCleanupException(ERROR_RESTORING_BACKUP + backupFileName, dbe);
        } finally {
            try {
                IoUtils.closeStream(backupReader, "Could not close reader for backup file "
                                                  + backupFileName);
                if (connection != null) {
                    connection.setAutoCommit(isAutoCommit);
                    connection.close();
                }
            } catch (SQLException sqle) {
                LOG.error(ERROR_RESTORING_BACKUP + backupFileName, sqle);
            }
        }
    }

    private void dropAndRecreateTable( Connection connection, String tableName ) {

        List<String> generateForeignKeysScripts = new ArrayList<String>();

        Map<String, List<String>> foreignKeys = getForeingKeys(tableName, connection);

        // generate script for restoring the exact table
        String generateTableScript = generateTableScript(tableName, connection);

        String scriptContent = loadScriptFromClasspath("generateForeignKeyScript.sql");

        // create the generateForeignKeyScript procedure
        createDatabaseProcedure(connection, scriptContent);

        for (Entry<String, List<String>> keyEntry : foreignKeys.entrySet()) {
            String parentTableName = keyEntry.getKey();
            for (String key : keyEntry.getValue()) {
                // generate scripts for creating all foreign keys
                generateForeignKeysScripts.add(generateForeignKeyScript(parentTableName, key,
                                                                        connection));
            }
        }

        // drop the newly created procedure
        executeUpdate("DROP PROCEDURE generateForeignKeyScript", connection);

        // drop the foreign keys
        dropForeignKeys(tableName, connection);
        // drop the table
        executeUpdate("DROP TABLE " + tableName + ";", connection);

        // create new table
        executeUpdate(generateTableScript, connection);
        // create all the missing foreign keys
        for (String script : generateForeignKeysScripts) {
            executeUpdate(script, connection);
        }
    }

    
    /**
     * Get file contents from classpath
     * @param scriptFileName Relative path is relative to the package of current class.
     * @return String of  
     */
    private String loadScriptFromClasspath(String scriptFileName) {
        String scriptContents = null;
        try (InputStream is = this.getClass().getResourceAsStream(scriptFileName)) {
            if (is != null) {
                scriptContents = IoUtils.streamToString(is);
            }
        } catch (Exception e) {
            if (e.getSuppressed() != null) { // possible close resources
                Throwable[] suppressedExc = e.getSuppressed();
                for (int i = 0; i < suppressedExc.length; i++) {
                    LOG.warn("Suppressed exception [" + i + "] details", suppressedExc[i]);
                }
            }
            throw new DbException("Could not load SQL server script needed for DB environment restore from classpath. Check "
                    + "location of " + scriptFileName + " file", e);
        }
        return scriptContents;
    }
    
    private Map<String, List<String>> getForeingKeys( String tableName,
                                                      Connection connection ) throws DbException {

        PreparedStatement stmnt = null;
        Map<String, List<String>> tableForeignKey = new HashMap<String, List<String>>();
        ResultSet rs = null;
        try {
            String simpleTableName = tableName.substring(tableName.indexOf('.') + 1, tableName.length());
            stmnt = connection.prepareStatement("EXEC sp_fkeys '" + simpleTableName + "'");
            rs = stmnt.executeQuery();
            while (rs.next()) {
                String fKey = rs.getString("FK_NAME");
                String parentTableName = rs.getString("FKTABLE_OWNER") + "."
                                         + rs.getString("FKTABLE_NAME");
                if (tableName.equals(parentTableName)) {
                    // this is the same table, the foreign key is created in the table creation script
                    continue;
                }
                if (tableForeignKey.containsKey(parentTableName)) {
                    if (!tableForeignKey.get(parentTableName).contains(fKey)) {
                        tableForeignKey.get(parentTableName).add(fKey);
                    }
                } else {
                    List<String> fKeys = new ArrayList<String>();
                    fKeys.add(fKey);
                    tableForeignKey.put(parentTableName, fKeys);
                }
            }

            return tableForeignKey;
        } catch (SQLException e) {
            throw new DbException(
                                  "SQL errorCode=" + e.getErrorCode() + " sqlState=" + e.getSQLState() + " "
                                  + e.getMessage(), e);
        } finally {
            DbUtils.closeStatement(stmnt);
        }
    }

    private void executeUpdate( String query, Connection connection ) throws DbException {

        PreparedStatement stmnt = null;
        try {
            stmnt = connection.prepareStatement(query);
            stmnt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Error executing statement: '" + query + "'\n"
                                  + "  SQL errorCode=" + e.getErrorCode() + " sqlState=" + e.getSQLState() + " "
                                  + e.getMessage(), e);
        } finally {
            DbUtils.closeStatement(stmnt);
        }
    }

    private void createDatabaseProcedure( Connection conn, String scriptContent ) {

        StringBuilder command = new StringBuilder();
        Statement stmt = null;
        
        String currentLine; 
        try (Scanner scanner = new Scanner(scriptContent)) {
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                currentLine = currentLine.trim();
                command.append(currentLine);
                command.append(" ");

                if (currentLine.endsWith("GO")) {
                    // commit the transaction
                    try {
                        stmt = conn.createStatement();
                        stmt.execute(command.toString());
                    } finally {
                        DbUtils.closeStatement(stmt);
                    }
                    command.setLength(0);
                }
            }
        } catch (Exception e) {
            String message = "Error while creating database procedure by running command: " + command;
            if (e instanceof SQLException) {
                throw new DbException(DbUtils.getFullSqlException(message, (SQLException) e));
            } else {
                throw new DbException(message, e);
            }
        }
        String commandStr = command.toString().trim(); 
        if (commandStr.length() > 0) {
            LOG.warn("Command '" + commandStr + "' will not be executed. If it is needed then add 'GO' statement at the end");
        }
    }

    private String generateForeignKeyScript( String tableName, String foreingKey,
                                             Connection connection ) throws DbException {

        CallableStatement callableStatement = null;
        ResultSet rs = null;
        try {
            callableStatement = connection.prepareCall("{ call generateForeignKeyScript(?,?) }");
            callableStatement.setString(1, tableName);
            callableStatement.setString(2, StringUtils.isNullOrEmpty(foreingKey)
                                                                                 ? null
                                                                                 : foreingKey);

            rs = callableStatement.executeQuery();
            String createQuery = new String();
            if (rs.next()) {
                createQuery = rs.getString(1);
            }

            return createQuery;

        } catch (Exception e) {
            throw new DbException("Error while generating script for the foreign keys of the table '"
                                  + tableName + "'.", e);
        } finally {
            DbUtils.closeStatement(callableStatement);
        }
    }

    private String generateTableScript( String tableName, Connection connection ) throws DbException {
        // script used from https://www.c-sharpcorner.com/UploadFile/67b45a/how-to-generate-a-create-table-script-for-an-existing-table/
        final String tableScriptFileName = "generateTableScript.sql";
        //String file = classLoader.getResource(tableName).getPath();
        
        String scriptContents = loadScriptFromClasspath(tableScriptFileName);
        
        // create the generateTableScript procedure
        createDatabaseProcedure( connection, scriptContents );
        
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        try {
            callableStatement = connection.prepareCall( "{ call generateTableScript(?) }" );
            callableStatement.setString( 1, tableName );

            rs = callableStatement.executeQuery();
            String createQuery = new String();
            if( rs.next() ) {
                createQuery = rs.getString( 1 );
            }
            
            return createQuery;

        } catch( Exception e ) {
            throw new DbException( "Error while generating script for the table '" + tableName + "'.", e );
        } finally {
            DbUtils.closeStatement( callableStatement );
            
            // drop the newly created procedure
            // TODO: drop procedure after last table drop invocation
            executeUpdate( "DROP PROCEDURE generateTableScript", connection );
            
        }
    }

    private void dropForeignKeys( String tableName, Connection connection ) throws DbException {

        String query = "SELECT "
                       + "'ALTER TABLE [' +  OBJECT_SCHEMA_NAME(parent_object_id) + "
                       + "'].[' + OBJECT_NAME(parent_object_id) + "
                       + "'] DROP CONSTRAINT [' + name + ']' "
                       + " FROM sys.foreign_keys "
                       + " WHERE referenced_object_id = object_id('" + tableName + "')";

        Statement callableStatement = null;
        ResultSet rs = null;
        try {
            callableStatement = connection.createStatement();
            rs = callableStatement.executeQuery(query);

            while (rs.next()) {
                executeUpdate(rs.getString(1), connection);
            }

        } catch (Exception e) {
            throw new DbException("Error while droping the foreign keys of table '" + tableName + "'.", e);
        } finally {
            DbUtils.closeStatement(callableStatement);
        }
    }
}
