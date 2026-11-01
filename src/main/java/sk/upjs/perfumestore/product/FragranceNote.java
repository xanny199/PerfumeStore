package sk.upjs.perfumestore.product;


import lombok.With;

@With
public record FragranceNote(
        Integer id,
        String name
) {}
