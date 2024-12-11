package io.github.qudtlib.support.index;

import java.util.Locale;
import java.util.Set;

public class SearchIndex<V> {

    private MultivaluePatriciaTrie<V> trie = new MultivaluePatriciaTrie();
    private final boolean caseInsensitive;

    public void clear() {
        trie.clear();
    }

    public SearchIndex(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public void put(String key, V value) {
        trie.put(key, value);
        if (this.caseInsensitive) {
            trie.put(key.toUpperCase(Locale.ROOT), value);
        }
    }

    public Set<V> get(String prefix, int flags) {
        String search = prefix;
        if (Flag.isCaseInsensitive(flags)) {
            search = search.toUpperCase(Locale.ROOT);
        }
        if (Flag.isMatchPrefix(flags)) {
            return trie.getByPrefixMatch(search);
        }
        return trie.get(search);
    }
}
