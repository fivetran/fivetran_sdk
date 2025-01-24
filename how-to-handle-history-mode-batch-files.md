# What is History Mode

History mode allows to capture every available version of each record from Fivetran source connectors.
In order to keep all versions of the records, three new system columns are added to tables with history mode enabled.


Column | Type | Description
--- | --- | ---
_fivetran_active | Boolean | TRUE if it is the currently active record. FALSE if it is a historical version of the record. Only one version of the record can be TRUE.
_fivetran_start | TimeStamp | The time when the record was first created or modified in the source.
_fivetran_end | TimeStamp | The value for this column depends on whether the record is active. If the record is not active, then `_fivetran_end` value will be `_fivetran_start` of the next version of the record minus 1 millisecond. If the record is deleted, then the value will be the same as the timestamp of delete operation. If the record is active, then `_fivetran_end` is set to maximum TIMESTAMP value.


### How to Maintain History?  

Following types of files are part of the `WriteHistoryBatchRequest` grpc call. These files need to be processed as described below and in the same order.  

#### `earliest_start_files`
- In `WriteHistoryBatchRequest`, we pass a new field, `earliest_start_files`. This file will consist of a single record for each primary key and the earliest value of the _fivetran_start value in the incoming batch.
  For this new file, perform following operations in order:
    - **Delete overlapping records** : 
        
      Delete query to remove any overlapping records where existing `_fivetran_start` is greater than the earliest_fivetran_start timestamp value in the `earliest_start_files` file.
  
        `DELETE FROM <schema.table> WHERE pk1 = <val> {AND  pk2 = <val>.....} AND _fivetran_start >= val(_earliest_fivetran_start);`

      - Update history mode columns: Updates `fivetran_active` and `fivetran_end` value in the destination. 
    
        `UPDATE <schema.table> SET fivetran_active = FALSE, _fivetran_end = t1 - 1 WHERE _fivetran_active = TRUE AND pk1 = <val> {AND  pk2 = <val>.....}`

#### `update_files`
- This file contains incomplete records where modified columns have actual values whereas unmodified columns have the special value `unmodified_string`. Before inserting these records in the table these records need to be completed. To complete the incoming records, wherever the value of the column is `unmodified_string` get the value from the last active row from the destination and fill that value in the unmodified column.

Suppose the existing table in destination is as follows:

Id(PK) | COL1 | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- 
1  | abc  | 1  | T1  | T2-1  | FALSE  | T100
1 | pqr | 2 | T2 | TMAX | TRUE | T101
2 | mno | 3 | T2 | TMAX | TRUE | T103


At the source, record with Id = 1 is updated:

Id(PK) | COL1 | Timestamp  | Type
--- | --- | --- | ---
1 | xyz | T3 | Updated



and record with Id = 2 is updated:

Id(PK) |  COL2  | Timestamp  | Type
--- | --- | --- | ---
2 | 1000 | T4 | Updated

And lastly, record with Id = 1 is again updated:

Id(PK) |  COL1  | Timestamp  | Type
--- | --- | --- | ---
1  | def  | T5  | Updated



The update batch file will be as follows:


Id(PK) | COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | --- 
1  | xyz | | T3| T5-1 | FALSE | T107
1 | def | | T5 | TMAX | TRUE | T109
2 | | 1000 | T4 | TMAX | TRUE | T108 


Final Destination Table will be as follows:

Id(PK) |  COL1  | COL2 | _fivetran_start(PK) | _fivetran_end | _fivetran_active | _fivetran_synced
--- | --- | --- | --- | --- | --- | ---
1  | abc  | 1  | T1  | T2-1  | FALSE  | T100
1  | pqr | 2 | T2 | T3-1 | FALSE | T101
2  | mno | 3 | T2 | T4-1 | FALSE | T103
1  | def | 2 | T5 | TMAX | TRUE | T109
1  | xyz | 2 | T3 | T5-1 | FALSE | T107
2  | mno | 1000 | T4 | TMAX | TRUE | T108


We set unmodified columns' values to the values of the active records. In this example, for id = 2, we didn’t get COL1 value, so we set COL1 to “mno” (COL1 value of the active record).

#### `upsert_files`
- For upsert files, we only need to insert the values. 


#### `delete_files`
- We set the `_fivetran_active` column value to FALSE for the active record and set the `_fivetran_end` column value in destination to value of the `_fivetran_end` in the batch file of the specified primary key in the row.


![History Mode Batch File](./history_mode.png) 