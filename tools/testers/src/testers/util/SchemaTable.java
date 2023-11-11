package testers.util;

import com.google.common.base.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;

public final class SchemaTable {
    public final String schema;
    public final String table;

    public SchemaTable(@Nonnull String schema, @Nonnull String table) {
        this.schema = schema;
        this.table = table;
    }

    @Override
    public String toString() {
        return schema + "." + table;
    }

    public String toString(Function<String, String> renamer) {
        return renamer.apply(schema) + "." + renamer.apply(table);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaTable that = (SchemaTable) o;
        return Objects.equal(schema, that.schema) && Objects.equal(table, that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schema, table);
    }
}
