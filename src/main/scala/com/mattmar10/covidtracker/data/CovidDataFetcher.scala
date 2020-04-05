package com.mattmar10.covidtracker.data

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import java.net._
import java.io._

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import scala.collection.mutable.{ListBuffer}
import com.amazonaws.services.s3.model.{
  ObjectMetadata,
  PutObjectRequest,
  PutObjectResult
}

class CovidDataFetcher extends RequestHandler[Object, String] {
  override def handleRequest(input: Object, context: Context): String = {

    assume(
      CovidDataFetcher.BucketName.nonEmpty,
      "BUCKET_NAME variable not defined"
    )

    CovidDataFetcher
      .handleRequest()
      .fold(ex => throw ex, succ => succ)
  }
}

object CovidDataFetcher {

  val CountyDataFile =
    "https://raw.githubusercontent.com/nytimes/covid-19-data/master/us-counties.csv"

  val StatesDataFile =
    "https://raw.githubusercontent.com/nytimes/covid-19-data/master/us-states.csv"

  val BucketName = sys.env.getOrElse("BUCKET_NAME", "") //"www.mattmartin.io"
  val DataFileName = "oklahoma-covid-data.csv"

  def handleRequest(): Either[Exception, String] = {

    try {
      val rows = fetchData()
      val result = writeToS3(rows)
      Right(result.getETag)
    } catch {
      case e: Exception => Left(e)
    }
  }

  def fetchData(): Seq[RawCountyDataRow] = {

    val listBuffer = new ListBuffer[RawCountyDataRow]

    val url = new URL(CountyDataFile)
    val in = new BufferedReader(new InputStreamReader(url.openStream))
    var inputLine = in.readLine
    while (inputLine != null) {
      if (inputLine.trim.contains("Oklahoma")) {
        val parts = inputLine.split(",")
        listBuffer.addOne(
          RawCountyDataRow(
            parts(0),
            parts(2),
            parts(1),
            parts(4).toInt,
            parts(5).toInt
          )
        )
      }
      inputLine = in.readLine
    }
    in.close

    listBuffer.toSeq
  }

  def writeToS3(data: Seq[RawCountyDataRow]): PutObjectResult = {

    val s3Client = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new EnvironmentVariableCredentialsProvider())
      .withRegion(Regions.US_EAST_1)
      .build();

    // Upload a file as a new object with ContentType and title specified.// Upload a file as a new object with ContentType and title specified.
    val stringWriter = new StringWriter()
    val bw = new BufferedWriter(stringWriter)
    bw.write("date,state,county,cases,deaths\n")

    data.foreach(
      row =>
        bw.write(
          s"${row.date},${row.state},${row.county},${row.cases},${row.deaths}\n"
      )
    )

    bw.flush()
    bw.close()

    println("writing to s3")
    val metadata = new ObjectMetadata
    metadata.setContentType("plain/text")
    metadata.addUserMetadata("title", "oklahoma-covid-data-counties.csv")
    val request =
      new PutObjectRequest(
        BucketName,
        DataFileName,
        new ByteArrayInputStream(stringWriter.toString.getBytes()),
        metadata
      )

    request.setMetadata(metadata)
    s3Client.putObject(request)
  }
}

case class RawCountyDataRow(date: String,
                            state: String,
                            county: String,
                            cases: Int,
                            deaths: Int)
