package sk.upjs.perfumestore.product;


import lombok.With;

@With
public record FragranceNote(
        Long id,
        String name
) {}
