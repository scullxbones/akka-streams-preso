package bootstrap.liftweb

class Boot {
  def boot(): Unit = {
    SnippetSetup.setup()
    MiscellaneousSetup.setup()
  }
}
