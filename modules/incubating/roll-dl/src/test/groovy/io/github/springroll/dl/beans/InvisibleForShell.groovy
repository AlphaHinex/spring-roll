package io.github.springroll.dl.beans

import org.springframework.stereotype.Component

@Component
class InvisibleForShell {

    def shouldNotBeInvokedInShell() {
        'This method SHOULD NOT be invoked in groovy shell execution'
    }

}
