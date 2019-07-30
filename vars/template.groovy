
def call(String fileName,Map options = [:]) {
  String fileContents = new File(fileName).text
  def engine = new groovy.text.SimpleTemplateEngine()
  def template = engine.createTemplate(text).make(options)
  println template.toString()
}
