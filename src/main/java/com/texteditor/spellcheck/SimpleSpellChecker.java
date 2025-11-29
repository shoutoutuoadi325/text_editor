package com.texteditor.spellcheck;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的内置拼写检查器适配器。
 * 
 * 使用适配器模式封装拼写检查逻辑，
 * 可以轻松替换为其他拼写检查后端（如 LanguageTool）。
 * 
 * 这个简单实现使用一个基本的英语单词词典进行检查。
 */
public class SimpleSpellChecker implements SpellChecker {
    
    // 常见英语单词词典
    private final Set<String> dictionary;
    
    // 匹配单词的正则表达式
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    public SimpleSpellChecker() {
        dictionary = new HashSet<>();
        initializeDictionary();
    }

    /**
     * 初始化基本词典。
     */
    private void initializeDictionary() {
        // 添加常见英语单词
        String[] commonWords = {
            // 冠词、代词、介词
            "a", "an", "the", "this", "that", "these", "those",
            "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "her", "its", "our", "their",
            "in", "on", "at", "to", "for", "of", "with", "by", "from", "about", "into",
            "through", "during", "before", "after", "above", "below", "between", "under",
            
            // 动词
            "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "done",
            "will", "would", "could", "should", "may", "might", "must", "can",
            "go", "goes", "went", "gone", "going",
            "get", "gets", "got", "getting",
            "make", "makes", "made", "making",
            "know", "knows", "knew", "known", "knowing",
            "think", "thinks", "thought", "thinking",
            "take", "takes", "took", "taken", "taking",
            "see", "sees", "saw", "seen", "seeing",
            "come", "comes", "came", "coming",
            "want", "wants", "wanted", "wanting",
            "use", "uses", "used", "using",
            "find", "finds", "found", "finding",
            "give", "gives", "gave", "given", "giving",
            "tell", "tells", "told", "telling",
            "work", "works", "worked", "working",
            "call", "calls", "called", "calling",
            "try", "tries", "tried", "trying",
            "ask", "asks", "asked", "asking",
            "need", "needs", "needed", "needing",
            "feel", "feels", "felt", "feeling",
            "become", "becomes", "became", "becoming",
            "leave", "leaves", "left", "leaving",
            "put", "puts", "putting",
            "mean", "means", "meant", "meaning",
            "keep", "keeps", "kept", "keeping",
            "let", "lets", "letting",
            "begin", "begins", "began", "begun", "beginning",
            "seem", "seems", "seemed", "seeming",
            "help", "helps", "helped", "helping",
            "show", "shows", "showed", "shown", "showing",
            "hear", "hears", "heard", "hearing",
            "play", "plays", "played", "playing",
            "run", "runs", "ran", "running",
            "move", "moves", "moved", "moving",
            "live", "lives", "lived", "living",
            "believe", "believes", "believed", "believing",
            "hold", "holds", "held", "holding",
            "bring", "brings", "brought", "bringing",
            "write", "writes", "wrote", "written", "writing",
            "provide", "provides", "provided", "providing",
            "sit", "sits", "sat", "sitting",
            "stand", "stands", "stood", "standing",
            "lose", "loses", "lost", "losing",
            "pay", "pays", "paid", "paying",
            "meet", "meets", "met", "meeting",
            "include", "includes", "included", "including",
            "continue", "continues", "continued", "continuing",
            "set", "sets", "setting",
            "learn", "learns", "learned", "learning",
            "change", "changes", "changed", "changing",
            "lead", "leads", "led", "leading",
            "understand", "understands", "understood", "understanding",
            "watch", "watches", "watched", "watching",
            "follow", "follows", "followed", "following",
            "stop", "stops", "stopped", "stopping",
            "create", "creates", "created", "creating",
            "speak", "speaks", "spoke", "spoken", "speaking",
            "read", "reads", "reading",
            "allow", "allows", "allowed", "allowing",
            "add", "adds", "added", "adding",
            "spend", "spends", "spent", "spending",
            "grow", "grows", "grew", "grown", "growing",
            "open", "opens", "opened", "opening",
            "walk", "walks", "walked", "walking",
            "win", "wins", "won", "winning",
            "offer", "offers", "offered", "offering",
            "remember", "remembers", "remembered", "remembering",
            "love", "loves", "loved", "loving",
            "consider", "considers", "considered", "considering",
            "appear", "appears", "appeared", "appearing",
            "buy", "buys", "bought", "buying",
            "wait", "waits", "waited", "waiting",
            "serve", "serves", "served", "serving",
            "die", "dies", "died", "dying",
            "send", "sends", "sent", "sending",
            "expect", "expects", "expected", "expecting",
            "build", "builds", "built", "building",
            "stay", "stays", "stayed", "staying",
            "fall", "falls", "fell", "fallen", "falling",
            "cut", "cuts", "cutting",
            "reach", "reaches", "reached", "reaching",
            "kill", "kills", "killed", "killing",
            "remain", "remains", "remained", "remaining",
            
            // 名词
            "time", "year", "people", "way", "day", "man", "woman",
            "child", "children", "world", "life", "hand", "part", "place",
            "case", "week", "company", "system", "program", "question",
            "work", "government", "number", "night", "point", "home", "water",
            "room", "mother", "area", "money", "story", "fact", "month",
            "lot", "right", "study", "book", "eye", "job", "word", "business",
            "issue", "side", "kind", "head", "house", "service", "friend",
            "father", "power", "hour", "game", "line", "end", "member",
            "law", "car", "city", "community", "name", "president", "team",
            "minute", "idea", "kid", "body", "information", "back", "parent",
            "face", "others", "level", "office", "door", "health", "person",
            "art", "war", "history", "party", "result", "change", "morning",
            "reason", "research", "girl", "guy", "moment", "air", "teacher",
            "force", "education", "foot", "boy", "age", "policy", "process",
            "music", "market", "sense", "nation", "plan", "college", "interest",
            "death", "experience", "effect", "use", "class", "control",
            "care", "field", "development", "role", "effort", "rate", "heart",
            "drug", "picture", "activity", "road", "form", "value", "action",
            "model", "family", "food", "bank", "period", "paper", "computer",
            "language", "term", "character", "site", "student", "problem",
            "data", "support", "view", "fire", "management", "player", "technology",
            "price", "author", "title",
            
            // 形容词
            "good", "new", "first", "last", "long", "great", "little",
            "own", "other", "old", "right", "big", "high", "different",
            "small", "large", "next", "early", "young", "important", "few",
            "public", "bad", "same", "able", "human", "local", "late",
            "hard", "major", "better", "economic", "strong", "possible",
            "whole", "free", "military", "true", "federal", "international",
            "full", "special", "easy", "clear", "recent", "certain", "personal",
            "open", "red", "difficult", "available", "likely", "short",
            "single", "medical", "current", "wrong", "private", "past",
            "foreign", "fine", "common", "poor", "natural", "significant",
            "similar", "hot", "dead", "central", "happy", "serious", "ready",
            "simple", "left", "physical", "general", "environmental", "financial",
            "blue", "democratic", "dark", "various", "entire", "close", "legal",
            "religious", "cold", "final", "main", "green", "nice", "huge",
            "popular", "traditional", "cultural", "quick", "fast", "slow",
            "beautiful", "wonderful", "amazing", "excellent", "perfect",
            "interesting", "important", "necessary", "successful", "basic",
            "normal", "professional", "standard", "complete", "complete",
            
            // 副词
            "up", "so", "out", "just", "now", "how", "then", "more",
            "also", "here", "well", "only", "very", "even", "back", "there",
            "down", "still", "in", "as", "too", "when", "never", "really",
            "most", "often", "always", "away", "however", "together", "likely",
            "simply", "generally", "instead", "actually", "almost", "especially",
            "ever", "today", "probably", "already", "below", "usually",
            "sometimes", "quickly", "slowly", "easily", "certainly",
            
            // 连词
            "and", "but", "or", "so", "because", "if", "when", "while",
            "although", "though", "unless", "since", "whether", "before", "after",
            
            // 数词
            "one", "two", "three", "four", "five", "six", "seven", "eight",
            "nine", "ten", "hundred", "thousand", "million", "billion",
            "first", "second", "third", "fourth", "fifth",
            
            // 其他常见词
            "not", "all", "no", "yes", "what", "who", "where", "why",
            "which", "each", "every", "any", "some", "many", "much",
            "such", "than", "like", "over", "both", "same", "another",
            
            // 书籍相关
            "book", "author", "title", "chapter", "page", "text", "story",
            "fiction", "novel", "poem", "poetry", "literature", "reader",
            "reading", "writer", "writing", "publish", "published", "publisher",
            "edition", "volume", "series", "collection", "library",
            
            // 食物相关
            "italian", "chinese", "japanese", "french", "mexican", "indian",
            "cooking", "food", "recipe", "meal", "dinner", "lunch", "breakfast",
            "restaurant", "kitchen", "chef", "cook", "bake", "grill",
            
            // 技术相关
            "xml", "html", "java", "python", "code", "program", "software",
            "hardware", "computer", "internet", "web", "website", "server",
            "database", "file", "folder", "document", "editor", "text",
            
            // 名字相关（常见英文名）
            "john", "james", "michael", "william", "david", "richard", "joseph",
            "thomas", "charles", "mary", "patricia", "jennifer", "linda", "elizabeth",
            "harry", "potter", "rowling", "giada", "laurentiis", "everyday",
            
            // 常用问候语和基本词汇
            "hello", "goodbye", "hi", "bye", "thanks", "please", "sorry",
            "test", "testing", "tested"
        };
        
        for (String word : commonWords) {
            dictionary.add(word.toLowerCase());
        }
    }

    @Override
    public List<SpellingError> checkText(String text) {
        List<SpellingError> errors = new ArrayList<>();
        String[] lines = text.split("\n");
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            Matcher matcher = WORD_PATTERN.matcher(line);
            
            while (matcher.find()) {
                String word = matcher.group();
                // 跳过单字母和纯数字
                if (word.length() <= 1) {
                    continue;
                }
                
                if (!isCorrect(word)) {
                    int column = matcher.start() + 1; // 1-indexed
                    List<String> suggestions = getSuggestions(word);
                    errors.add(new SpellingError(lineNum + 1, column, word, suggestions));
                }
            }
        }
        
        return errors;
    }

    @Override
    public boolean isCorrect(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    @Override
    public List<String> getSuggestions(String word) {
        List<String> suggestions = new ArrayList<>();
        String lowerWord = word.toLowerCase();
        
        // 使用简单的编辑距离算法查找相似单词
        for (String dictWord : dictionary) {
            if (editDistance(lowerWord, dictWord) <= 2) {
                suggestions.add(dictWord);
                if (suggestions.size() >= 3) {
                    break;
                }
            }
        }
        
        return suggestions;
    }

    /**
     * 计算两个字符串的编辑距离（Levenshtein Distance）。
     */
    private int editDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        // 优化：如果长度差太大，直接返回大值
        if (Math.abs(m - n) > 2) {
            return 3;
        }
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                   Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[m][n];
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
