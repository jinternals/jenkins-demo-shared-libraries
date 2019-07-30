
def call(String fileName,Map options = [:]) {
  der template = readFile fileName
  def engine = new groovy.text.SimpleTemplateEngine()
  def result = engine.createTemplate(template).make(options)
  println result.toString()
}
