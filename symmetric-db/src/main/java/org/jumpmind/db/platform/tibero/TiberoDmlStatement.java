package org.jumpmind.db.platform.tibero;

import java.sql.Types;

import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.TypeMap;
import org.jumpmind.db.platform.DatabaseInfo;
import org.jumpmind.db.sql.DmlStatement;

public class TiberoDmlStatement extends DmlStatement {

    public TiberoDmlStatement(DmlType type, String catalogName, String schemaName, String tableName,
            Column[] keysColumns, Column[] columns, boolean[] nullKeyValues, 
            DatabaseInfo databaseInfo, boolean useQuotedIdentifiers, String textColumnExpression) {
        super(type, catalogName, schemaName, tableName, keysColumns, columns, 
                nullKeyValues, databaseInfo, useQuotedIdentifiers, textColumnExpression);
    }
   
    @Override
    protected void appendColumnParameter(StringBuilder sql, Column column) {
        String name = column.getJdbcTypeName();
        if (column.isTimestampWithTimezone()) {
            sql.append("TO_TIMESTAMP_TZ(?, 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')")
                    .append(",");
        } else if (name != null && (
                name.toUpperCase().contains(TypeMap.GEOMETRY) || 
                name.toUpperCase().contains(TypeMap.GEOGRAPHY))) {
            sql.append("SYM_WKT2GEOM(?)").append(",");
        } else {
            super.appendColumnParameter(sql, column);
        }
    }
    
    @Override
    protected void appendColumnEquals(StringBuilder sql, Column column) {
        if (column.isTimestampWithTimezone()) {
            sql.append(quote).append(column.getName()).append(quote)
                    .append(" = TO_TIMESTAMP_TZ(?, 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')");
        } else if (column.getJdbcTypeName().toUpperCase().contains(TypeMap.GEOMETRY) ||
                column.getJdbcTypeName().toUpperCase().contains(TypeMap.GEOGRAPHY)) {
            sql.append(quote).append(column.getName()).append(quote).append(" = ")
                    .append("SYM_WKT2GEOM(?)");
        } else {
            super.appendColumnEquals(sql, column);
        }        
    }

    @Override
    protected int getTypeCode(Column column, boolean isDateOverrideToTimestamp) {
        int typeCode = super.getTypeCode(column, isDateOverrideToTimestamp);
        if (column.getJdbcTypeName().startsWith("XML")) {
            typeCode = Types.VARCHAR;
        } else if (typeCode == Types.LONGVARCHAR) {
            typeCode = Types.CLOB;
        }
        return typeCode;
    }
    
    @Override
    protected void appendColumnNameForSql(StringBuilder sql, Column column, boolean select) {
        String columnName = column.getName();
        if (select && column.isTimestampWithTimezone()) {
            sql.append("to_char(").append(quote).append(columnName).append(quote).append(", 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM') as ").append(columnName);
        } else {
            super.appendColumnNameForSql(sql, column, select);
        }        
    }


}
