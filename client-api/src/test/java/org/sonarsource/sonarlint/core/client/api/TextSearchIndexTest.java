package org.sonarsource.sonarlint.core.client.api;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextSearchIndexTest {
  private TextSearchIndex<String> index;

  @Before
  public void setUp() {
    index = new TextSearchIndex<>();
  }

  @Test
  public void testTokenizer() {
    index.index("o1", "org.sonarsource.sonarlint.intellij:sonarlint-intellij SonarLint Intellij");
    index.index("o2", "org.codehaus.sonar-plugins:sonar-scm-jazzrtc-plugin Jazz RTC SCM Plugin");

    assertThat(index.getTokens()).contains("org", "sonarsource", "sonarlint", "intellij",
      "codehaus", "sonar", "plugins", "scm", "jazzrtc", "plugin", "jazz", "rtc");
  }

  @Test
  public void testSearch() {
    index.index("o1", "org.sonarsource.sonarlint.intellij:sonarlint-intellij SonarLint Intellij");
    index.index("o2", "org.codehaus.sonar-plugins:sonar-scm-jazzrtc-plugin Jazz RTC SCM Plugin");

    assertThat(index.search("org")).containsOnly("o1", "o2");
    assertThat(index.search("jazzrtc")).containsOnly("o2");
    assertThat(index.search("sonarlint")).containsOnly("o1");
    assertThat(index.search("plugin")).containsOnly("o2");
    assertThat(index.search("scm")).containsOnly("o2");
    assertThat(index.search("org.sonarsource.sonarlint.intellij:sonarlint-intellij")).contains("o1");
    assertThat(index.search("unknown")).isEmpty();
  }

  @Test
  public void clear() {
    index.index("o1", "org.sonarsource.sonarlint.intellij:sonarlint-intellij SonarLint Intellij");
    index.index("o2", "org.codehaus.sonar-plugins:sonar-scm-jazzrtc-plugin Jazz RTC SCM Plugin");

    index.clear();

    assertThat(index.getTokens()).isEmpty();
    assertThat(index.search("org")).isEmpty();

    // no errors
    index.index("o1", "org.sonarsource.sonarlint.intellij:sonarlint-intellij SonarLint Intellij");
    index.index("o2", "org.codehaus.sonar-plugins:sonar-scm-jazzrtc-plugin Jazz RTC SCM Plugin");
  }

  @Test(expected = IllegalArgumentException.class)
  public void cantIndexTwice() {
    index.index("o1", "a");
    index.index("o1", "b");
  }

  @Test
  public void testMultiTermPositionalSearch() {
    index.index("o1", "org.sonarsource.sonarlint.intellij:sonarlint-intellij SonarLint Intellij");
    index.index("o2", "org.codehaus.sonar-plugins:sonar-scm-jazzrtc-plugin Jazz RTC SCM Plugin");

    assertThat(index.search("sonar-plugins")).contains("o2");

    // multi term matches full terms only
    assertThat(index.search("sona-plugins")).isEmpty();

    // distance is 1
    assertThat(index.search("org.plugins")).isEmpty();
  }

  @Test
  public void testScoring() {
    index.index("o1", "A A A B B C");
    index.index("o2", "A A A B B C C");
    index.index("o3", "A A B B B C D");

    assertThat(index.search("A")).containsExactly("o1", "o2", "o3");
    assertThat(index.search("B")).containsExactly("o3", "o1", "o2");
    assertThat(index.search("C")).containsExactly("o2", "o1", "o3");
  }
}
