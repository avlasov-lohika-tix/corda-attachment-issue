# How to test
1. Create database
```shell
docker run --name mylocal -p 5432:5432\ 
  -e POSTGRES_PASSWORD=postgres\ 
  -e POSTGRES_USER=postgres\ 
  -e POSTGRES_MULTIPLE_DATABASES=buyer,funder,supplier\ 
  -d postgres
```
2. Run gradle task for deploy nodes
```shell
gradle deployNodes
```
```shell
./gradlew deployNodes
```
3. Execute `runnodes` command
```shell
./workflow/nodes/runnodes
```
4. Verify that all the node started
   1. Notary
   2. Funder
   3. Buyer
   4. Supplier
5. Run a flow on the Funder node
```shell
 start org.example.test.workflow.input.state.AttachmentInputStateTestFlow$AttachmentInputStateTestUpload search: "test", party: "O=supplier, L=London, C=GB"
```