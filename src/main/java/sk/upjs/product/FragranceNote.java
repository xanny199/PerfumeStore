package sk.upjs.product;


import lombok.With;

@With
public record FragranceNote(
        Long id,
        String name
) {}
