package com.rm.myrecipes.ui.fragments.recipes

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.jakewharton.espresso.OkHttp3IdlingResource
import com.rm.myrecipes.R
import com.rm.myrecipes.data.di.UrlModule
import com.rm.myrecipes.launchFragmentInHiltContainer
import com.rm.myrecipes.util.MockWebSerDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(UrlModule::class)
@HiltAndroidTest
class RecipesFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var okHttp: OkHttpClient

    private lateinit var okHttp3IdlingResource: OkHttp3IdlingResource


    @Before
    fun init() {
        hiltRule.inject()
        mockWebServer = MockWebServer()
        okHttp3IdlingResource = OkHttp3IdlingResource.create("okhttp", okHttp)
        IdlingRegistry.getInstance().register(okHttp3IdlingResource)
        mockWebServer.dispatcher = MockWebSerDispatcher().RequestDispatcher()
        mockWebServer.start(8080)

    }

    @Test(expected = NoMatchingViewException::class)
    fun fragmentTestOnErrorNoElementsInRecyclerView() {
        launchFragmentInHiltContainer<RecipesFragment>(themeResId = R.style.BaseAppTheme) {
            this.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
        }

        onView(withId(R.id.fabRecipes)).perform(click())

        onView(withId(R.id.btnApply)).perform(click())

        onView(withId(R.id.rvRecipeFragment)).check { view, noViewException ->
            if (noViewException != null) {
                throw noViewException
            }
        }
    }

    fun fragmentTestRecyclerViewItems() {
        launchFragmentInHiltContainer<RecipesFragment>(themeResId = R.style.BaseAppTheme) {
            this.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
        }

        onView(withId(R.id.fabRecipes)).perform(click())

        onView(withId(R.id.btnApply)).perform(click())

        onView(withId(R.id.rvRecipeFragment)).check { view, noViewException ->
            if (noViewException != null) {
                throw noViewException
            }
            val recyclerView = view as RecyclerView
            assertEquals(20, recyclerView.adapter?.itemCount)
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class FakeBaseUrlModule {
        @Provides
        @Singleton
        fun provideUrl(): String = "http://127.0.0.1:8080/"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(okHttp3IdlingResource)
    }
}