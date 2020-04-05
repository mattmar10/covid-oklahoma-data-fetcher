
sbt universal:packageBin

sam package --template-file template.yaml --s3-bucket mattmartin-covid-data --output-template-file packaged.yaml

sam deploy --template-file ./packaged.yaml --stack-name covid-data-lambda --capabilities CAPABILITY_IAM --parameter-overrides BucketName=www.mattmartin.io
