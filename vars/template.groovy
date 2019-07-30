
def call(String fileName,Map options = [:]) {
  def templateContent = readFile fileName
  def engine = new groovy.text.SimpleTemplateEngine()
  def result = engine.createTemplate(templateContent).make(options)
  println result.toString()
}
