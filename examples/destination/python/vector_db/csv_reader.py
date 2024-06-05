import pandas as pd
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import zstandard as zstd
from io import StringIO
from logger import log


class CSVReaderAESZSTD:

    def read_csv(self, file_path, aes_key, null_string, timestamp_columns):
        with open(file_path, 'rb') as encrypted_file:
            iv = encrypted_file.read(16)
            encrypted_data = encrypted_file.read()

        cipher = AES.new(aes_key, AES.MODE_CBC, iv)
        decrypted_data = unpad(cipher.decrypt(encrypted_data), AES.block_size)

        decompressor = zstd.ZstdDecompressor()

        with decompressor.stream_reader(decrypted_data) as reader:
            decompressed_data = reader.read()

        data_str = decompressed_data.decode('utf-8')
        df = pd.read_csv(StringIO(data_str), na_values=null_string, parse_dates=timestamp_columns)
        return df