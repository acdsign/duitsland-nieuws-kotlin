package io.supersimple.duitslandnieuws.data.repositories.article

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.requery.Persistable
import io.requery.kotlin.eq
import io.requery.reactivex.KotlinReactiveEntityStore
import io.supersimple.duitslandnieuws.data.models.Article

class ArticleDisk(private val store: KotlinReactiveEntityStore<Persistable>) {

    fun get(id: String): Maybe<Article> = getDAO(id).map { convertFromDb(it) }

    fun save(article: Article): Single<Article> = Single.just(article)
            .map { convertToDb(it) }
            .flatMap { store.upsert(it) }
            .map { convertFromDb(it) }

    fun list(page: Int, pageSize: Int): Maybe<List<Article>> =
            store.select(ArticleDAO::class)
                    .orderBy(ArticleDAOEntity.DATE.desc())
                    .limit(pageSize)
                    .offset(page * pageSize)
                    .get()
                    .observable()
                    .map { convertFromDb(it) }
                    .toList()
                    .filter { it.isNotEmpty() }

    fun save(articles: List<Article>): Single<List<Article>> =
            Observable.fromIterable(articles)
                    .flatMapSingle { save(it) }
                    .toList()

    fun delete(article: Article): Single<Article> = delete(article.id)

    fun delete(id: String): Single<Article> =
            getDAO(id)
                    .toSingle()
                    .flatMap { deleteArticleDAO(it) }
                    .map { convertFromDb(it) }

    fun deleteAll(): Single<Int> =
            store.delete(ArticleDAO::class)
                    .get()
                    .single()

    private fun deleteArticleDAO(article: ArticleDAO): Single<ArticleDAO> =
            store.delete(article)
                    .toSingle<ArticleDAO> { article }

    private fun getDAO(id: String): Maybe<ArticleDAO> =
            store.select(ArticleDAO::class)
                    .where(ArticleDAO::id eq id)
                    .get()
                    .maybe()

    companion object {

        fun convertFromDb(dbArticle: ArticleDAO): Article =
                Article(dbArticle.id,
                        dbArticle.date,
                        dbArticle.modified,
                        dbArticle.slug,
                        dbArticle.link,
                        dbArticle.title,
                        dbArticle.content,
                        dbArticle.excerpt,
                        dbArticle.author,
                        dbArticle.featured_media)

        fun convertToDb(article: Article): ArticleDAO {
            val o = ArticleDAOEntity()
            o.setId(article.id)
            o.setDate(article.date)
            o.setModified(article.modified)
            o.setSlug(article.slug)
            o.setLink(article.link)
            o.setTitle(article.title)
            o.setContent(article.content)
            o.setExcerpt(article.excerpt)
            o.setAuthor(article.author)
            o.setFeatured_media(article.featured_media)
            return o
        }
    }
}