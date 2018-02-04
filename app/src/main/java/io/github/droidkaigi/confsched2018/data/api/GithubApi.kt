package io.github.droidkaigi.confsched2018.data.api

import android.support.annotation.CheckResult
import io.github.droidkaigi.confsched2018.data.api.response.Contributor
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {
    @GET("/repos/{owner}/{repo}/contributors")
    @CheckResult
    fun getContributors(@Path("owner") owner: String, @Path("repo") repo: String,
                        @Query("per_page") perPage: Int, @Query("page") next: Int? = null):
            Single<List<Contributor>>
}
