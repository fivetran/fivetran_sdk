from datetime import datetime
from abc import ABC, abstractmethod

from models.collection import Collection, Row
from typing import Optional, Any, List

from sdk.common_pb2 import ConfigurationFormResponse


class VectorDestination(ABC):
    """Interface for vector destinations"""

    @abstractmethod
    def configuration_form(self) -> ConfigurationFormResponse:
        """configuration_form"""

    @abstractmethod
    def test(self, name: str, configuration: dict[str, str]) -> Optional[str]:
        """test"""

    @abstractmethod
    def create_collection_if_not_exists(self, configuration: dict[str, Any], collection: Collection) -> None:
        """create_collection"""

    @abstractmethod
    def upsert_rows(self, configuration: dict[str, Any], collection: Collection, rows: List[Row]) -> None:
        """upsert_rows"""

    @abstractmethod
    def delete_rows(self, configuration: dict[str, Any], collection: Collection, ids: List[str]) -> None:
        """delete_rows"""

    @abstractmethod
    def truncate(self, configuration: dict[str, Any], collection: Collection, synced_column: str, delete_before: datetime) -> None:
        """delete_rows"""

    # Not Ideal but no clear winner ¯\_(ツ)_/¯
    def get_collection_name(self, schema_name: str, table_name: str) -> str:
        schema_name = schema_name.replace("_","-")
        table_name = table_name.replace("_", "-")
        return f"{schema_name}-{table_name}"
