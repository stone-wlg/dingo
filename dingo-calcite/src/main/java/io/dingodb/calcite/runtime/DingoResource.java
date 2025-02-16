/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.calcite.runtime;

import io.dingodb.common.exception.DingoSqlException;
import org.apache.calcite.runtime.Resources;

import static org.apache.calcite.runtime.Resources.BaseMessage;
import static org.apache.calcite.runtime.Resources.ExInst;

public interface DingoResource {
    DingoResource DINGO_RESOURCE = Resources.create(DingoResource.class);

    @BaseMessage("Primary keys are required in table ''{0}''")
    ExInst<DingoSqlException> primaryKeyRequired(String a0);

    @BaseMessage("Specified primary keys ''{0}'' does not exist in table ''{1}''")
    ExInst<DingoSqlException> primaryKeyNotExist(String pkName, String tableName);

    @BaseMessage("''{0}'' command denied to user ''{1}''@''{2}'' for table ''{3}''")
    ExInst<DingoSqlException> operatorDenied(String a0, String a1, String a2, String a3);

    @BaseMessage("Access denied; you need (at least one of) the CREATE USER privilege(s) for this operation")
    ExInst<DingoSqlException> createUserDenied();

    @BaseMessage("Invalid default value for ''{0}''")
    ExInst<DingoSqlException> invalidDefaultValue(String a0);

    @BaseMessage("Invalid schema value for ''{0}''")
    ExInst<DingoSqlException> invalidSchemaName(String a0);

    @BaseMessage("Invalid table value for ''{0}''")
    ExInst<DingoSqlException> invalidTableName(String a0);

    @BaseMessage("Invalid column value for ''{0}''")
    ExInst<DingoSqlException> invalidColumnName(String a0);

    @BaseMessage("Incorrect column specifier for column ''{0}''")
    ExInst<DingoSqlException> specifierForColumn(String a0);

    @BaseMessage("There is no such grant defined for user ''{0}'' on host ''{1}''")
    ExInst<DingoSqlException> noDbGrantsForRevoke(String a0, String a1);

    @BaseMessage("There is no such grant defined for user ''{0}'' on host ''{1}'' on table ''{2}''")
    ExInst<DingoSqlException> noTableGrantsForRevoke(String a0, String a1, String a2);

    @BaseMessage("Operation CREATE USER failed for ''{0}''@''{1}''")
    ExInst<DingoSqlException> createUserFailed(String a0, String a1);

    @BaseMessage("Operation DROP USER failed for ''{0}''@''{1}''")
    ExInst<DingoSqlException> dropUserFailed(String a0, String a1);

    @BaseMessage("Operation ALTER USER failed for ''{0}''@''{1}''")
    ExInst<DingoSqlException> alterUserFailed(String a0, String a1);

    @BaseMessage("Can't find any matching row in the user table")
    ExInst<DingoSqlException> noMatchingRowForUser();

    @BaseMessage("Access denied for user ''{0}''@''{1}''")
    ExInst<DingoSqlException> accessDeniedToUser(String a0, String a1);

    @BaseMessage("Access denied for user ''{0}''@''{1}'' to database ''{2}''")
    ExInst<DingoSqlException> accessDeniedToDb(String a0, String a1, String a2);

    @BaseMessage("Error 1051(42S02): Unknown schema ''{0}''")
    ExInst<DingoSqlException> unknownSchema(String a0);

    @BaseMessage("Unknown character set ''{0}''")
    ExInst<DingoSqlException> unknownCharacterSet(String a0);

    @BaseMessage("Error 1051(42S02): Unknown table ''{0}''")
    ExInst<DingoSqlException> unknownTable(String a0);

    @BaseMessage("Error 1051(42S02): Unknown index ''{0}''")
    ExInst<DingoSqlException> unknownIndex(String a0);

    @BaseMessage("Error 1231(42000): Variable ''{0}'' can not be set to the value ''{1}''")
    ExInst<DingoSqlException> invalidVariableArg(String a0, String a1);

    @BaseMessage("Illegal column name definition")
    ExInst<DingoSqlException> invalidColumn();

    @BaseMessage("Incorrect argument type to variable ''{0}''")
    ExInst<DingoSqlException> incorrectArgType(String a0);

    @BaseMessage("Transaction characteristics can't be changed while a transaction is in progress")
    ExInst<DingoSqlException> transChangeError();
    @BaseMessage("Can not get stat of ''{0}'' (Errcode: ''{1}'' - ''{2}'')")
    ExInst<DingoSqlException> accessError(String a0, int a1, String a2);

    @BaseMessage("Can not create/write to file ''{0}'' (Errcode: 13 - Permission denied)")
    ExInst<DingoSqlException> writeReadError(String a0);

    @BaseMessage("Incorrect value")
    ExInst<DingoSqlException> loadDataError();

    @BaseMessage("Duplicate entry for key 'PRIMARY'")
    ExInst<DingoSqlException> duplicateKey();

    @BaseMessage("Error 1086(HY000): File ''{0}'' already exists")
    ExInst<DingoSqlException> exportFileExists(String a0);

    @BaseMessage("Error 1083(42000): Field/Line separator argument is not what is expected; check the manual")
    ExInst<DingoSqlException> fieldSeparatorError();

    @BaseMessage("Error 1146(42S02): Table ''{0}'' does not exist")
    ExInst<DingoSqlException> tableNotExists(String a0);

    @BaseMessage("Tenant ''{0}'' already exists")
    ExInst<DingoSqlException> tenantExists(String a0);

    @BaseMessage("Tenant ''{0}'' not found")
    ExInst<DingoSqlException> tenantNotFound(String a0);

    @BaseMessage("Error 1060 (42S21): Duplicate column names are not allowed in table definition")
    ExInst<DingoSqlException> duplicateColumn();

    @BaseMessage("Error 1105 (HY000): Check illegalArgumentException")
    ExInst<DingoSqlException> illegalArgumentException();

    @BaseMessage("Error 9001 (45000): Not enough regions is created")
    ExInst<DingoSqlException> notEnoughRegion();

    @BaseMessage("Error 9001 (45000): Not allowed to set 0 replicas")
    ExInst<DingoSqlException> notAllowedZeroReplica();

    @BaseMessage("Error 9001 (45000): table definition illegal, multi auto_increment column")
    ExInst<DingoSqlException> multiAutoInc();

    @BaseMessage("Error 8200 (HY000): can't drop column name with composite index covered or Primary Key covered now")
    ExInst<DingoSqlException> dropColumnError();

    @BaseMessage("Error 8200 (HY000): unsupported add column ''{0}'' constraint PRIMARY KEY when altering ''{1}''")
    ExInst<DingoSqlException> addColumnPrimaryError(String columnName, String tableName);

    @BaseMessage("Error 8200 (HY000): unsupported add column ''{0}'' constraint AUTO_INCREMENT when altering ''{1}''")
    ExInst<DingoSqlException> addColumnAutoIncError(String columnName, String tableName);

    @BaseMessage("Error 1050 (42S01): Table ''{0}'' already exists")
    ExInst<DingoSqlException> tableExists(String name);

    @BaseMessage("Error 1091 (42000): Can not DROP ''{0}''; check that column/key exists")
    ExInst<DingoSqlException> dropColumnNotExists(String name);

    @BaseMessage("Error 1410 (42000): You are not allowed to create a user with GRANT")
    ExInst<DingoSqlException> grantError();
    @BaseMessage("Sequence ''{0}'' already exists")
    ExInst<DingoSqlException> sequenceExists(String name);

    @BaseMessage("Error 8135 (HY000): invalid as of timestamp: as of timestamp can't be set in transaction.")
    ExInst<DingoSqlException> invalidAsTimestamp();

    @BaseMessage("Error 9006 (HY000): GC life time is shorter than transaction duration")
    ExInst<DingoSqlException> invalidAsTimestampParam();

    @BaseMessage("Error 1054 (42S22): Unknown column ''{0}'' in ''{1}''")
    ExInst<DingoSqlException> unknownColumn(String name, String tableName);

    @BaseMessage("Error 8200 (HY000): Unsupported modify column: can't set auto_increment")
    ExInst<DingoSqlException> modifyColumnError1();

}
