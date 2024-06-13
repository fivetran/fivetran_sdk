mkdir -p proto
for file in ../../../*v2.proto; do
    file_name=$(basename "$file")
    new_file="proto/${file_name%_v2.proto}.proto"
    cp "$file" "$new_file"
done