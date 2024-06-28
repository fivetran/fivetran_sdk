import grpc
from concurrent import futures
from sdk.destination_sdk_pb2_grpc import add_DestinationServicer_to_server
from destination import VectorDestination
from service import VectorDestinationServicer
import sys


def serve(vec_dest: VectorDestination):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_DestinationServicer_to_server(VectorDestinationServicer(vec_dest), server)

    if len(sys.argv) == 3 and sys.argv[1] == '--port':
        port = int(sys.argv[2])
    else:
        port = 50052

    server.add_insecure_port(f'[::]:{port}')
    print(f"Running GRPC Server on {port}")
    server.start()
    server.wait_for_termination()


from destinations.weaviate_ import WeaviateDestination

if __name__ == '__main__':
    serve(WeaviateDestination())
