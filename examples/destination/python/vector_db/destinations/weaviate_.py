import uuid
import weaviate
from weaviate.auth import AuthApiKey
from weaviate.classes.query import Filter

from sdk.common_pb2 import ConfigurationFormResponse, FormField, TextField, ConfigurationTest
from destination import VectorDestination


class WeaviateDestination(VectorDestination):
    def get_collection_name(self, schema_name, table_name):
        return f"{schema_name}_{table_name}"

    def configuration_form(self):
        fields = [
            FormField(name="url", label="Weaviate Cluster URL", required=True, text_field=TextField.PlainText),
            FormField(name="api_key", label="Weaviate API Key", required=True, text_field=TextField.Password)
        ]
        tests = [ConfigurationTest(name="connection_test", label="Connecting to Weaviate Cluster")]
        return ConfigurationFormResponse(fields=fields, tests=tests)

    def _get_client(self, configuration):
        return weaviate.connect_to_wcs(
            cluster_url=configuration["url"],
            auth_credentials=AuthApiKey(configuration["api_key"])
        )

    def test(self, name, config):
        if name != "connection_test":
            raise ValueError(name)

        client = self._get_client(config)

        client.connect()
        client.close()

    def create_collection_if_not_exists(self, config, collection):
        client = self._get_client(config)

        if not client.collections.exists(collection.name):
            print(f"Collection {collection.name} does not exist! Creating!")
            client.collections.create(name=collection.name)

        client.close()

    def upsert_rows(self, config, collection, rows):
        client = self._get_client(config)
        c = client.collections.get(collection.name)

        for row in rows:
            _uuid = uuid.uuid5(uuid.NAMESPACE_DNS, str(row.id))

            # TODO: Get these swap column names from config
            # TODO: swap column name in framework if other vec dbs have same issue.
            id_swap_column = "_fvt_swp_id"
            vector_swap_column = "_fvt_swp_vector"

            if "id" in row.payload:
                row.payload[id_swap_column] = row.payload.pop("id")
            if "vector" in row.payload:
                row.payload[vector_swap_column] = row.payload.pop("vector")

            if c.data.exists(_uuid):
                c.data.replace(uuid=_uuid, properties=row.payload, vector=row.vector)
            else:
                c.data.insert(uuid=_uuid, properties=row.payload, vector=row.vector)

        client.close()

    def delete_rows(self, config, collection, ids):
        client = self._get_client(config)
        c = client.collections.get(collection.name)

        for id in ids:
            _uuid = uuid.uuid5(uuid.NAMESPACE_DNS, str(id))
            c.data.delete_by_id(uuid=_uuid)

        client.close()

    def truncate(self, config, collection, synced_column, delete_before):
        client = self._get_client(config)
        c = client.collections.get(collection.name)

        filter = Filter.by_property(synced_column).less_than(delete_before)
        c.data.delete_many(where=filter)

