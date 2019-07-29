def call() {

  sh "git tag --sort version:refname | tail -1 > version.tmp"

  def tag = readFile 'version.tmp'
  
   if (tag == null || tag.size() == 0){
      echo "no existing tag found using version ${version}"
      return ""
   }

   return tag.trim()
 
} 
