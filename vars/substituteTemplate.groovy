
def call(String inputFile , Map options = [:], String outputFile) {
  def templateContent = readFile inputFile
  def engine = new groovy.text.SimpleTemplateEngine()
  def result = engine.createTemplate(templateContent).make(options)
  writeFile outputFile result.toString()
}
