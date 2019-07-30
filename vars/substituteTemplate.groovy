def call(String inputFile , Map options = [:], String outputFile) {
  transform(inputFile, options, outputFile)
}

def transform(String inputFile , Map options = [:], String outputFile) {
  def templateContent = readFile inputFile
  def engine = new groovy.text.SimpleTemplateEngine()
  def result = engine.createTemplate(templateContent).make(options)
  def templateOutput = result.toString()
 
  engine = null
  result = null
  writeFile file: outputFile, text: templateOutput
  sh "ls"
}
