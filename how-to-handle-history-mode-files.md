#### What is History Mode

History mode allows us to capture every version of each record processed by the fivetran connectors.
In order to keep all versions of the record, we have introduced three new system columns for tables with history mode enabled.


Column | Type | Description
--- | --- | ---
_fivetran_active | Boolean | TRUE if it is the currently active record. FALSE if it is a historical version of the record. Only one version of the record can be TRUE.
_fivetran_start | TimeStamp | The time when the record was first created or modified in the source.
_fivetran_end | TimeStamp | The value for this column depends on whether the record is active. If the record is not active(`_fivetran_active`=FALSE), then `_fivetran_end` value will be `_fivetran_start` of the next version of the record minus 1 millisecond. If the record is deleted, then the value will be the same as the timestamp of delete operation. If the record is active(`_fivetran_active`=TRUE), then `_fivetran_end` is the max allowed value that we can set for a TIMESTAMP column.


#### Points to remember in history mode

- In WriterBatchRequest we pass a new optional field HistoryMode which indicates connector is in history mode or not. In this HistoryMode field, we pass `deleted_column` column name which we need to modify only if it is present in destination.   
- If the existing table is not empty then in the batch file we also send a boolean column `_fivetran_earliest`. Suppose in an `upsert` we got multiple versions of the same record in a flush, then we set the `_fivetran_earliest` to `TRUE` for the record which have the earliest `_fivetran_start` and rest of the versions will have `_fivetran_earliest` as FALSE.
- For each Replace, Update and Delete batch files, DELETE the existing records from destination table if `_fivetran_start` of destination table is greater than or equal to  `_fivetran_start` of batch file(Refer Replace example 1 and example 2).

Note: The `_fivetran_earliest` column shouldn't be added in the destination table. It is introduced to easily identify the earliest record and can be used to optimize data loads query.
Below is an example of `replace_file`

Id(PK) | COL1    | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_earliest
---|---------|---------------------| --- |------------------| --- 
1 | abc     | T1                  | T2-1  | FALSE            | TRUE
2 | xyz     | T1                  | TMAX | TRUE             | TRUE
1 | pqr     | T2                  | T3-1 | FALSE            | FALSE
1 | def | T3                  | TMAX                | TRUE             | FALSE

#### How to Handle Replaces, Updates and Deletes

##### Replace

###### Example 1:

When `_fivetran_start` of destination table is less than  `_fivetran_start` of batch file.
Suppose the existing Table in destination is as below:

Id(PK) | COL1 | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- |------|----| --- | --- | --- | ---
1 | abc  | 1  |T1 | T2-1 | FALSE | T100
1 | pqr | 2  |  T2 | TMAX | TRUE | T101
2 | mno | 3  | T2  | TMAX | TRUE | T103

At source new records are added:

Id(PK) | COL1 | COL2 | Timestamp | Type
--- | --- | --- |-----------| ---
1 | def |1 | T3        | Inserted
1 | ghi | 1 |  T4       | Inserted

Replace batch file will be:

Id(PK) | COL1 | COL2  | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_earliest | _fivetran_synced
--- |------|-------|---------------------| --- | --- | --- | ---
1 | def  | 1     | T3                  | T4-1 | FALSE | TRUE | T104
1 | ghi  | 1| T4                  | TMAX | TRUE | FALSE | T105


Final Destination Table will be:

Id(PK) | COL1 | COL2   | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- |---|--------|---------------------| --- |------------------| ---
1  | abc | 1      | T1                  | T2-1 | FALSE            | T100
1 | pqr | 2      | T2                  | T3-1 | FALSE            | T101
2  | mno  | 3  | T3                  | TMAX | TRUE             | T103
1 | def | 1 |T3 | T4-1 | FALSE            | T104
1  | ghi | 1 | T4 | TMAX | TRUE             | T105

**Explanation:**
- We got new records for id = 1. 
- Check for corresponding earliest record(`_fivetran_earliest` as TRUE), DELETE the existing records from destination table if `_fivetran_start` of destination table is greater than or equal to  `_fivetran_start` of batch file(In above example no)
- `_fivetran_end` of the active record in destination table is set to `_fivetran_start`-1 of the `_fivetran_earliest` record of batch file.
- Set `_fivetran_active` for above updated record to FALSE and `deleted_column`(if present in destination table) to TRUE
- New records are inserted AS IS excluding `_fivetran_earliest` column in destination table.

###### Example 2

When `_fivetran_start` of destination table is greater than or equal to  `_fivetran_start` of batch file.
Suppose the existing Table in destination is as below:

Id(PK) | COL1 | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- |---|--------|---------------------| --- |------------------| ---
1  | xyz | 4 | T1 | T3-1 | FALSE            | T100
1  | abc | 1 | T3 | T4-1 | FALSE            | T100 
1  | pqr | 2 | T4 | TMAX | TRUE             | T101
2  | mno | 3 | T4 | TMAX | TRUE             | T103

At source new records are added:

Id(PK) | COL1 | COL2  | Timestamp  | Type
--- | --- | --- | --- | ---
1 | ghi | 1  | T2  | Inserted



Replace batch file will be:

Id(PK) | COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_earliest | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- | ---
1  | ghi | 1 | T2 | TMAX | TRUE | TRUE | T104

Final Destination table will be:

Id(PK) |  COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- 
1  | ghi | 1 | T2 | TMAX | TRUE | T104
1  | xyz | 4 | T1 | T3-1 | FALSE | T100
2  | mno | 3 | T4 | TMAX | TRUE | T103

**Explanation:**
We got new records for id = 1.
- Check for corresponding earliest record(`_fivetran_earliest` TRUE), DELETE the existing records from destination table if `_fivetran_start` of destination table is greater than or equal to  `_fivetran_start` of batch file(in above example yes, so deleted id = 1 with _fivetran_start = T3 and T4)
- `_fivetran_end` of the active record in destination table is set to `_fivetran_start`-1 of the `_fivetran_earliest` record of batch file.
- Set `_fivetran_active` for above updated record to FALSE and `deleted_column`(if present in destination table) to TRUE
- New records are inserted AS IS excluding `_fivetran_earliest` column in destination table.

##### Updates

Suppose the existing Table in destination is:

Id(PK) | COL1 | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- 
1  | abc  | 1  | T1  | T2-1  | FALSE  | T100
1 | pqr | 2 | T2 | TMAX | TRUE | T101
2 | mno | 3 | T2 | TMAX | TRUE | T103


At source records with Id = 1 is updated:

Id(PK) | COL1 | Timestamp  | Type
--- | --- | --- | ---
1 | xyz | T3 | Updated



And record with id = 2 is updated as:

Id(PK) |  COL2  | Timestamp  | Type
--- | --- | --- | ---
2 | 1000 | T4 | Updated

And record with Id = 1 is again updated as

Id(PK) |  COL1  | Timestamp  | Type
--- | --- | --- | ---
1  | def  | T5  | Updated



Update batch file will be:


Id(PK) | COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_earliest | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- | ---
1  | xyz | | T3| T5-1 | FALSE | TRUE | T107
2 | | 1000 | T4 | TMAX | TRUE | TRUE | T108
1 | def | | T5 | TMAX | TRUE | FALSE | T109


Final Destination Table will be:

Id(PK) |  COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | ---
1  | abc  | 1  | T1  | T2-1  | FALSE  | T100
1  | pqr | 2 | T2 | T3-1 | FALSE | T101
2  | mno | 3 | T2 | T4-1 | FALSE | T103
1  | def | 2 | T5 | TMAX | TRUE | T109
1  | xyz | 2 | T3 | T5-1 | FALSE | T107
2  | mno | 1000 | T4 | TMAX | TRUE | T108



**Explanation:**
 - In batch file we got records with id = 1 and id = 2.
- We set other columns(non updated columns) to the values of the active records. In above case for id = 2, we didn’t get COL1 value, so we set COL1 to “mno”(COL1 value of the active record)
 - _fivetran_end of the active record in destination table is set to _fivetran_start-1 of the _fivetran_earliest record of batch file
- Set _fivetran_active for above updated record to FALSE and deleted_column(if present in destination table) to TRUE
- Other columns are set AS IS from the batch file in the destination table except _fivetran_earliest column.


##### Deletes

Existing Table in destination:

Id(PK) | COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | ---
1  | abc | 1  | T1  | T2-1 | FALSE | T100
1  | pqr | 2 | T2 | TMAX | TRUE | T101
2 | mno | 3 | T2 | TMAX | TRUE | T103



At source a record is deleted:


Id(PK) | Timestamp  | Type
--- | --- | ---
1  | T3 | Deleted


Delete batch file will be:

Id(PK) | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_earliest | _fivetran_synced
--- | --- |---------------|------| --- | ---
1  | | T3-1          |  | TRUE | T104

    
Final Destination Table will be:

Id(PK) | COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- |------------------| ---
1  | abc  | 1  | T1  | T2-1  | FALSE            | T100
1  | pqr | 2 | T2 | T3-1 | FALSE            | T101
2  | mno | 3 | T2 | TMAX | TRUE             | T103

**Explanation:**
Set `_fivetran_active` to FALSE for the active record and set `_fivetran_end` = T3-1 and `deleted_column`(if present in destination) to TRUE


