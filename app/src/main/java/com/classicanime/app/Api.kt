package com.classicanime.app
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class Title(val romaji: String?)
data class Cover(val large: String?)
data class SDate(val year: Int?)
data class Link(val site: String?, val url: String?, val type: String?)
data class Media(
    val id: Int, val title: Title?, val coverImage: Cover?, val bannerImage: String?,
    val description: String?, val startDate: SDate?, val genres: List<String>?,
    val format: String?, val externalLinks: List<Link>?
)
data class MediaPage(val media: List<Media>?)
data class PageData(val Page: MediaPage?)
data class Resp(val data: PageData?)

interface AniList { @POST("/") suspend fun query(@Body body: Map<String, @JvmSuppressWildcards Any>): Resp }

object Api {
    val svc: AniList = Retrofit.Builder().baseUrl("https://graphql.anilist.co/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(AniList::class.java)
    const val Q = "query(\$from:FuzzyDateInt,\$to:FuzzyDateInt){Page(page:1,perPage:20){media(type:ANIME,startDate_greater:\$from,startDate_lesser:\$to,sort:POPULARITY_DESC,format_in:[TV,MOVIE]){id title{romaji} coverImage{large} bannerImage description(asHtml:false) startDate{year} genres format externalLinks{site url type}}}}"
}
