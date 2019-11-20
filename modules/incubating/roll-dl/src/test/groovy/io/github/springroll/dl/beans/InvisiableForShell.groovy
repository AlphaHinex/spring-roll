package io.github.springroll.dl.beans

import org.springframework.stereotype.Component

@Component
class InvisiableForShell {

    def shouldNotBeInvokedInShell() {
        'This method SHOULD NOT be invoked in groovy shell execution'
    }

}
