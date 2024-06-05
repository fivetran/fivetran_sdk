from abc import ABC, abstractmethod

from typing import Optional, Any, List, Tuple

from sdk.common_pb2 import ConfigurationFormResponse
from models.collection import Metrics


class Embedder(ABC):
    """Interface for embedders"""

    @abstractmethod
    def details(self)-> Tuple[str, str]:
        """details -> [id, name]"""


    @abstractmethod
    def configuration_form(self) -> ConfigurationFormResponse:
        """configuration_form"""

    @abstractmethod
    def metrics(self, configuration: dict[str, str])-> Metrics:
        """metrics"""

    @abstractmethod
    def test(self, name: str, configuration: dict[str, str]) -> Optional[str]:
        """test"""

    @abstractmethod
    def embed(self, configuration: dict[str, str], texts: List[str]) -> List[List[float]]:
        """embed"""
