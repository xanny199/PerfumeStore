package sk.upjs.perfumestore.product;


import lombok.With;
import java.math.BigDecimal;
import java.util.List;


@With
public record Product(
        Long id,
        String name,
        String brand,
        CategoryType categoryType,
        TargetAudience targetAudience,
        String description,
        Integer volumeML,
        BigDecimal price,
        Integer releaseYear,
        Integer currentStock,
        Boolean isAvailable,
        List<FragranceNote> notes
) {
    public enum CategoryType {
        PARFUM,
        COSMETICS,
        HOME_FRAGRANCE
    }

    public enum TargetAudience {
        MEN,
        WOMEN,
        UNISEX
    }
}
