from embedder import Embedder
from sdk.common_pb2 import ConfigurationFormResponse, FormField, TextField, DropdownField, ConfigurationTest
from langchain_openai import OpenAIEmbeddings
from models.collection import Metrics, Distance

MODELS = {
    "text-embedding-ada-002": Metrics(
        distance=Distance.COSINE,
        dimensions=1536
    )
}


class OpenAIEmbedder(Embedder):

    def details(self):
        return "open_ai", "OpenAI"

    def configuration_form(self):
        models = DropdownField(dropdown_field=list(MODELS.keys()))
        fields = [
            FormField(name="api_key", label="OpenAI API Key", required=True, text_field=TextField.Password),
            FormField(name="embedding_model", label="OpenAI Embedding Model", required=True, dropdown_field=models),
        ]
        tests = [ConfigurationTest(name="embedding_test", label="Checking OpenAI Embedding Generation")]
        return ConfigurationFormResponse(fields=fields, tests=tests)

    def metrics(self, config) -> Metrics:
        return MODELS[config["embedding_model"]]

    def _get_embedding(self, configuration):
        api_key = configuration["api_key"]
        model = configuration["embedding_model"]

        return OpenAIEmbeddings(api_key=api_key, model=model)

    def test(self, name, configuration):
        if name != "embedding_test":
            raise ValueError(f'Unknown test : {name}')

        embedding = self._get_embedding(configuration)
        embedding.embed_query("foo-bar-biz")

    def embed(self, configuration, texts):
        embedding = self._get_embedding(configuration)
        return embedding.embed_documents(texts)
