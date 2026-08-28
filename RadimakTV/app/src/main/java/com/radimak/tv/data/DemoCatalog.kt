package com.radimak.tv.data

import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.MediaType

object DemoCatalog {
    val movies = listOf(
        CatalogItem(
            id = -101,
            title = "Duna: Parte Dois",
            overview = "Paul Atreides se une a Chani e aos Fremen enquanto busca impedir um futuro terrível que somente ele consegue prever.",
            rating = 8.2,
            releaseDate = "2024-02-27",
            mediaType = MediaType.MOVIE,
            genres = listOf("Ficção científica", "Aventura"),
            accentColor = 0xFFB45309,
        ),
        CatalogItem(-102, "Furiosa", "Uma jovem é arrancada de seu lar e precisa sobreviver a um mundo em colapso.", rating = 7.5, releaseDate = "2024-05-22", mediaType = MediaType.MOVIE, genres = listOf("Ação", "Aventura"), accentColor = 0xFFDC2626),
        CatalogItem(-103, "Garfield", "O gato mais famoso do mundo embarca em uma aventura inesperada.", rating = 7.1, releaseDate = "2024-04-30", mediaType = MediaType.MOVIE, genres = listOf("Animação", "Comédia"), accentColor = 0xFFF59E0B),
        CatalogItem(-104, "Bad Boys 4", "Mike e Marcus enfrentam uma nova missão cheia de ação e humor.", rating = 7.3, releaseDate = "2024-06-05", mediaType = MediaType.MOVIE, genres = listOf("Ação", "Comédia"), accentColor = 0xFF1D4ED8),
        CatalogItem(-105, "O Dublê", "Um dublê precisa encontrar uma estrela desaparecida enquanto tenta reconquistar o amor de sua vida.", rating = 7.4, releaseDate = "2024-04-24", mediaType = MediaType.MOVIE, genres = listOf("Ação", "Comédia"), accentColor = 0xFFEA580C),
        CatalogItem(-106, "Godzilla e Kong", "Dois titãs encaram uma ameaça colossal escondida nas profundezas do planeta.", rating = 7.2, releaseDate = "2024-03-27", mediaType = MediaType.MOVIE, genres = listOf("Ação", "Ficção científica"), accentColor = 0xFF0F766E),
        CatalogItem(-107, "Divertida Mente 2", "Novas emoções chegam à mente de Riley durante a adolescência.", rating = 7.9, releaseDate = "2024-06-11", mediaType = MediaType.MOVIE, genres = listOf("Animação", "Família"), accentColor = 0xFF7C3AED),
        CatalogItem(-108, "Planeta dos Macacos", "Um novo líder constrói um império enquanto outro jovem questiona tudo o que aprendeu.", rating = 7.1, releaseDate = "2024-05-08", mediaType = MediaType.MOVIE, genres = listOf("Aventura", "Drama"), accentColor = 0xFF3F6212),
    )

    val series = listOf(
        CatalogItem(-201, "The Bear", "Um jovem chef retorna a Chicago para administrar a lanchonete de sua família.", rating = 8.6, releaseDate = "2022-06-23", mediaType = MediaType.SERIES, genres = listOf("Drama", "Comédia"), accentColor = 0xFF92400E),
        CatalogItem(-202, "Shōgun", "Poder, guerra e lealdade se cruzam no Japão do século XVII.", rating = 8.7, releaseDate = "2024-02-27", mediaType = MediaType.SERIES, genres = listOf("Drama", "História"), accentColor = 0xFF991B1B),
        CatalogItem(-203, "The Last of Us", "Joel e Ellie atravessam um mundo devastado em busca de esperança.", rating = 8.6, releaseDate = "2023-01-15", mediaType = MediaType.SERIES, genres = listOf("Drama", "Aventura"), accentColor = 0xFF365314),
        CatalogItem(-204, "Loki", "O deus da trapaça enfrenta linhas do tempo que ameaçam toda a realidade.", rating = 8.2, releaseDate = "2021-06-09", mediaType = MediaType.SERIES, genres = listOf("Fantasia", "Aventura"), accentColor = 0xFF166534),
        CatalogItem(-205, "Fallout", "Sobreviventes deixam abrigos subterrâneos e encontram um mundo estranho e violento.", rating = 8.3, releaseDate = "2024-04-10", mediaType = MediaType.SERIES, genres = listOf("Ficção científica", "Aventura"), accentColor = 0xFF1E3A8A),
        CatalogItem(-206, "House of the Dragon", "A Casa Targaryen mergulha em uma disputa que ameaça dividir o reino.", rating = 8.4, releaseDate = "2022-08-21", mediaType = MediaType.SERIES, genres = listOf("Fantasia", "Drama"), accentColor = 0xFF7F1D1D),
        CatalogItem(-207, "Bridgerton", "Amor, reputação e segredos movimentam a alta sociedade londrina.", rating = 8.1, releaseDate = "2020-12-25", mediaType = MediaType.SERIES, genres = listOf("Drama", "Romance"), accentColor = 0xFFBE185D),
        CatalogItem(-208, "O Urso e a Garota", "Uma história de amizade, escolhas e recomeços.", rating = 7.8, releaseDate = "2024-01-12", mediaType = MediaType.SERIES, genres = listOf("Drama"), accentColor = 0xFF4338CA),
    )

    val all = movies + series
}
