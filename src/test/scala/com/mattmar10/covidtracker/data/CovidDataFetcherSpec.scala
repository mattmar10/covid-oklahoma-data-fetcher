package com.mattmar10.covidtracker.data

import org.scalatest.{MustMatchers, WordSpec}

class CovidDataFetcherSpec extends WordSpec with MustMatchers {

  "CovidDataFetcher" must {

    "cotain an element " in {
      val fetched = CovidDataFetcher.fetchData()

      fetched must contain(
        RawCountyDataRow("2020-03-06", "Oklahoma", "Tulsa", 1, 0)
      )

      CovidDataFetcher.writeToS3(fetched)
    }
  }
}
