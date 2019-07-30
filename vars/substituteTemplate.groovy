def call(String inputFile , Map options = [:], String outputFile) {
  _transform(inputFile, options, outputFile)
}

def _transform(String inputFile , Map binding = [:], String outputFile) {
  def input = readFile inputFile
  def engine = new org.apache.commons.lang3.text.StrSubstitutor(binding)
  def templateOutput = engine.replace(input)
  engine = null
  writeFile file: outputFile, text: templateOutput
}
