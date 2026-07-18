package com.cheonjiin.kbd

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

/* ============================================================
   천지인 한글 오토마타 (웹 프로토타입에서 이식)
   ============================================================ */
object Han {
    val CHO = listOf("ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ")
    val JUNG = listOf("ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ","ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ")
    val JONG = listOf("","ㄱ","ㄲ","ㄳ","ㄴ","ㄵ","ㄶ","ㄷ","ㄹ","ㄺ","ㄻ","ㄼ","ㄽ","ㄾ","ㄿ","ㅀ","ㅁ","ㅂ","ㅄ","ㅅ","ㅆ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ")

    val JONG_SPLIT = mapOf(
        "ㄳ" to listOf("ㄱ","ㅅ"), "ㄵ" to listOf("ㄴ","ㅈ"), "ㄶ" to listOf("ㄴ","ㅎ"),
        "ㄺ" to listOf("ㄹ","ㄱ"), "ㄻ" to listOf("ㄹ","ㅁ"), "ㄼ" to listOf("ㄹ","ㅂ"),
        "ㄽ" to listOf("ㄹ","ㅅ"), "ㄾ" to listOf("ㄹ","ㅌ"), "ㄿ" to listOf("ㄹ","ㅍ"),
        "ㅀ" to listOf("ㄹ","ㅎ"), "ㅄ" to listOf("ㅂ","ㅅ")
    )
    val JONG_COMBINE = mapOf(
        "ㄱㅅ" to "ㄳ","ㄴㅈ" to "ㄵ","ㄴㅎ" to "ㄶ","ㄹㄱ" to "ㄺ","ㄹㅁ" to "ㄻ",
        "ㄹㅂ" to "ㄼ","ㄹㅅ" to "ㄽ","ㄹㅌ" to "ㄾ","ㄹㅍ" to "ㄿ","ㄹㅎ" to "ㅀ","ㅂㅅ" to "ㅄ"
    )

    // 천지인 자음 멀티탭 사이클
    val CONS = mapOf(
        "ㄱㅋ" to listOf("ㄱ","ㅋ","ㄲ"), "ㄴㄹ" to listOf("ㄴ","ㄹ"), "ㄷㅌ" to listOf("ㄷ","ㅌ","ㄸ"),
        "ㅂㅍ" to listOf("ㅂ","ㅍ","ㅃ"), "ㅅㅎ" to listOf("ㅅ","ㅎ","ㅆ"), "ㅈㅊ" to listOf("ㅈ","ㅊ","ㅉ"),
        "ㅇㅁ" to listOf("ㅇ","ㅁ")
    )
    private val JONG_VALID = JONG.drop(1).toSet()
    val CONS_JONG: Map<String, List<String>> = CONS.mapValues { (_, v) -> v.filter { JONG_VALID.contains(it) } }

    // 모음 오토마타 전이표
    val VT: Map<String, Map<String, String>> = mapOf(
        "" to mapOf("ㅣ" to "I", "ㆍ" to "D1", "ㅡ" to "EU"),
        "I" to mapOf("ㆍ" to "A", "ㅣ" to "__NEWI", "ㅡ" to "__COMMIT"),
        "EU" to mapOf("ㅣ" to "UI", "ㆍ" to "U", "ㅡ" to "__COMMIT"),
        "D1" to mapOf("ㅣ" to "EO", "ㅡ" to "O", "ㆍ" to "D2"),
        "D2" to mapOf("ㅣ" to "YEO", "ㅡ" to "YO", "ㆍ" to "D2"),
        "A" to mapOf("ㆍ" to "YA", "ㅣ" to "AE", "ㅡ" to "__COMMIT"),
        "YA" to mapOf("ㅣ" to "YAE", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "EO" to mapOf("ㅣ" to "E", "ㆍ" to "YEO", "ㅡ" to "__COMMIT"),
        "YEO" to mapOf("ㅣ" to "YE", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "O" to mapOf("ㅣ" to "OE", "ㆍ" to "O_D1", "ㅡ" to "__COMMIT"),
        "O_D1" to mapOf("ㅣ" to "WA", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "OE" to mapOf("ㆍ" to "WA", "ㅣ" to "__NEWI", "ㅡ" to "__COMMIT"),
        "WA" to mapOf("ㅣ" to "WAE", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "U" to mapOf("ㅣ" to "WI", "ㆍ" to "YU", "ㅡ" to "__COMMIT"),
        "YU" to mapOf("ㅣ" to "WEO", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "U_D1" to mapOf("ㅣ" to "WEO", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "WI" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "WEO" to mapOf("ㅣ" to "WE", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "WE" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "UI" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "AE" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "E" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "YAE" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "YE" to mapOf("ㅣ" to "__NEWI", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT"),
        "YO" to mapOf("ㅣ" to "__COMMIT", "ㆍ" to "__COMMIT", "ㅡ" to "__COMMIT")
    )
    val SJ: Map<String, String?> = mapOf(
        "I" to "ㅣ","EU" to "ㅡ","A" to "ㅏ","YA" to "ㅑ","EO" to "ㅓ","YEO" to "ㅕ","O" to "ㅗ",
        "YO" to "ㅛ","U" to "ㅜ","YU" to "ㅠ","AE" to "ㅐ","YAE" to "ㅒ","E" to "ㅔ","YE" to "ㅖ",
        "UI" to "ㅢ","OE" to "ㅚ","WI" to "ㅟ","WA" to "ㅘ","WAE" to "ㅙ","WEO" to "ㅝ","WE" to "ㅞ",
        "O_D1" to "ㅗ","U_D1" to "ㅜ","D1" to null,"D2" to null
    )

    data class VStep(val commit: Boolean, val state: String, val jung: String?)

    fun vowelStep(state: String, sym: String): VStep {
        val row = VT[state]
        var nxt = row?.get(sym) ?: "__COMMIT"
        if (nxt == "__COMMIT") { val ns = VT[""]!![sym]!!; return VStep(true, ns, SJ[ns]) }
        if (nxt == "__NEWI") return VStep(true, "I", "ㅣ")
        return VStep(false, nxt, SJ[nxt])
    }

    fun buildChar(cho: String?, jung: String?, jong: String?): String {
        if (cho != null && jung != null) {
            val ci = CHO.indexOf(cho); val ji = JUNG.indexOf(jung)
            val ki = if (jong != null && jong.isNotEmpty()) JONG.indexOf(jong) else 0
            if (ci >= 0 && ji >= 0 && ki >= 0) return (0xAC00 + (ci * 21 + ji) * 28 + ki).toChar().toString()
        }
        if (cho != null && jung == null) return cho + (jong ?: "")
        if (cho == null && jung != null) return jung
        return ""
    }
}

/* ============================================================
   조합 버퍼 (한 음절). commit 문자열을 돌려주면 IME가 확정 처리.
   ============================================================ */
class Composer {
    var cho: String? = null
    var jung: String? = null
    var jong: String? = null
    var vstate: String = ""
    var vtaps = ArrayList<String>()
    var mtKey: String? = null
    var mtIdx = 0
    var mtSlot: String? = null
    var mtBase: String = ""

    fun reset() { cho = null; jung = null; jong = null; vstate = ""; vtaps.clear(); mtKey = null; mtIdx = 0; mtSlot = null; mtBase = "" }
    fun isEmpty() = cho == null && jung == null && jong == null
    fun render() = Han.buildChar(cho, jung, jong)
    fun lockCons() { mtKey = null; mtIdx = 0; mtSlot = null; mtBase = "" }

    private fun clearAll() { cho = null; jung = null; jong = null; vstate = ""; vtaps.clear() }

    private fun jongCands(base: String, key: String): List<String> {
        val jc = Han.CONS_JONG[key] ?: emptyList()
        if (base.isEmpty()) return jc
        val out = ArrayList<String>()
        for (c in jc) { val combo = Han.JONG_COMBINE[base + c]; if (combo != null) out.add(combo) }
        return out
    }

    fun inputConsonant(key: String): String? {
        val cyc = Han.CONS[key] ?: return null
        var commit: String? = null
        if (jung == null) {
            if (cho == null) { cho = cyc[0]; mtKey = key; mtIdx = 0; mtSlot = "cho"; mtBase = "" }
            else if (mtKey == key && mtSlot == "cho") { mtIdx = (mtIdx + 1) % cyc.size; cho = cyc[mtIdx] }
            else { commit = render(); clearAll(); cho = cyc[0]; mtKey = key; mtIdx = 0; mtSlot = "cho"; mtBase = "" }
            vstate = ""; vtaps.clear()
        } else if (jong == null) {
            if (cho == null) {
                // 초성 없는 모음(예: ㅏ) 뒤 자음 → 받침으로 붙이지 말고 새 음절 시작
                commit = render(); clearAll(); cho = cyc[0]; mtKey = key; mtIdx = 0; mtSlot = "cho"; mtBase = ""
            } else {
                val cand = jongCands("", key)
                if (cand.isNotEmpty()) { jong = cand[0]; mtKey = key; mtIdx = 0; mtSlot = "jong"; mtBase = "" }
                else { commit = render(); clearAll(); cho = cyc[0]; mtKey = key; mtIdx = 0; mtSlot = "cho"; mtBase = "" }
            }
        } else {
            if (mtKey == key && mtSlot == "jong") {
                val cand = jongCands(mtBase, key)
                if (cand.isNotEmpty()) { mtIdx = (mtIdx + 1) % cand.size; jong = cand[mtIdx] }
            } else {
                val cand = jongCands(jong!!, key)
                if (cand.isNotEmpty()) { val base = jong!!; jong = cand[0]; mtKey = key; mtIdx = 0; mtSlot = "jong"; mtBase = base }
                else { commit = render(); clearAll(); cho = cyc[0]; mtKey = key; mtIdx = 0; mtSlot = "cho"; mtBase = "" }
            }
        }
        return commit
    }

    private fun applyVowel(sym: String): String? {
        val r = Han.vowelStep(vstate, sym)
        return if (r.commit) {
            val committed = render()
            cho = null; jung = r.jung; jong = null; vstate = r.state; vtaps = arrayListOf(sym)
            committed
        } else {
            jung = r.jung; vstate = r.state; vtaps.add(sym); null
        }
    }

    fun inputVowel(sym: String): String? {
        mtKey = null; mtSlot = null
        if (jong != null) {
            val split = Han.JONG_SPLIT[jong!!]
            val remain = split?.get(0)
            val moved = split?.get(1) ?: jong!!
            val committed = Han.buildChar(cho, jung, remain)
            clearAll(); cho = moved
            applyVowel(sym)
            return committed
        }
        return applyVowel(sym)
    }

    // true = 조합 영역에서 처리됨, false = 앞의 확정 글자를 지워야 함
    fun backspace(): Boolean {
        if (isEmpty()) return false
        if (jong != null) {
            jong = Han.JONG_SPLIT[jong!!]?.get(0)
            mtKey = null; mtSlot = null
            return true
        }
        if (jung != null) {
            if (vtaps.isNotEmpty()) vtaps.removeAt(vtaps.size - 1)
            if (vtaps.isEmpty()) { jung = null; vstate = "" }
            else { var st = ""; for (s in vtaps) st = Han.VT[st]?.get(s) ?: ""; vstate = st; jung = Han.SJ[st] }
            return true
        }
        if (cho != null) { cho = null; mtKey = null; mtSlot = null }
        return true
    }
}

/* ============================================================
   IME 서비스
   ============================================================ */
class CheonjiinIME : InputMethodService() {

    private val composer = Composer()
    private var mode = "ko"
    private var symReturn = "ko"
    private var capsOn = false
    private val wordBuf = StringBuilder()
    private var suggestionBar: LinearLayout? = null

    private val lockHandler = Handler(Looper.getMainLooper())
    private val lockRunnable = Runnable { composer.lockCons() }
    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun ic() = currentInputConnection

    override fun onCreate() {
        super.onCreate()
        Thread { Corrector.ensureLoaded() }.start()
    }

    override fun onCreateInputView(): View = buildKeyboardView()

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        composer.reset(); wordBuf.setLength(0)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        ic()?.finishComposingText()
        composer.reset(); wordBuf.setLength(0)
    }

    private fun armLock() {
        lockHandler.removeCallbacks(lockRunnable)
        lockHandler.postDelayed(lockRunnable, 650)
    }
    private fun setComposing() { ic()?.setComposingText(composer.render(), 1) }

    private fun onCons(key: String) {
        val commit = composer.inputConsonant(key)
        if (commit != null) { ic()?.commitText(commit, 1); wordBuf.append(commit) }
        setComposing(); armLock(); updateSuggestions()
    }
    private fun onVowel(sym: String) {
        lockHandler.removeCallbacks(lockRunnable)
        val commit = composer.inputVowel(sym)
        if (commit != null) { ic()?.commitText(commit, 1); wordBuf.append(commit) }
        setComposing(); updateSuggestions()
    }
    private fun flushComposing() {
        val c = ic() ?: return
        if (!composer.isEmpty()) { val r = composer.render(); c.commitText(r, 1); wordBuf.append(r) }
        else c.finishComposingText()
        composer.reset()
        lockHandler.removeCallbacks(lockRunnable)
    }
    private fun onBackspace() {
        val c = ic() ?: return
        lockHandler.removeCallbacks(lockRunnable)
        if (composer.backspace()) {
            c.setComposingText(composer.render(), 1)
        } else {
            c.deleteSurroundingText(1, 0)
            if (wordBuf.isNotEmpty()) wordBuf.deleteCharAt(wordBuf.length - 1)
        }
        updateSuggestions()
    }
    private fun onSpace() {
        flushComposing(); maybeAutoCorrect(); ic()?.commitText(" ", 1)
        wordBuf.setLength(0); updateSuggestions()
    }
    private fun onEnter() {
        flushComposing(); maybeAutoCorrect()
        val c = ic() ?: return
        val action = (currentInputEditorInfo?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) c.performEditorAction(action)
        else c.commitText("\n", 1)
        wordBuf.setLength(0); updateSuggestions()
    }
    private fun onPunct(p: String) {
        flushComposing(); maybeAutoCorrect(); ic()?.commitText(p, 1)
        wordBuf.setLength(0); updateSuggestions()
    }
    private fun onEnglish(ch: String) {
        flushComposing()
        val s = if (capsOn) ch.uppercase() else ch
        ic()?.commitText(s, 1); wordBuf.append(s)
        if (capsOn) { capsOn = false; setInputView(buildKeyboardView()) }
        updateSuggestions()
    }
    private fun maybeAutoCorrect() {
        val word = wordBuf.toString()
        if (word.isEmpty()) return
        val cand = Corrector.autoCorrect(word, mode) ?: return
        val c = ic() ?: return
        c.deleteSurroundingText(word.length, 0)
        c.commitText(cand, 1)
        wordBuf.setLength(0); wordBuf.append(cand)
    }

    private fun toggleLang() {
        flushComposing(); wordBuf.setLength(0)
        mode = if (mode == "en") "ko" else "en"; capsOn = false
        setInputView(buildKeyboardView())
    }
    private fun toggleSym() {
        flushComposing(); wordBuf.setLength(0)
        if (mode != "sym") { symReturn = mode; mode = "sym" } else { mode = symReturn }
        capsOn = false
        setInputView(buildKeyboardView())
    }

    private fun updateSuggestions() {
        val bar = suggestionBar ?: return
        bar.removeAllViews()
        val word = wordBuf.toString() + (if (mode == "ko") composer.render() else "")
        if (word.isEmpty() || mode == "sym") return
        val cands = Corrector.suggest(word, mode)
        for (cw in cands) {
            val b = Button(this)
            b.text = cw; b.isAllCaps = false
            b.setTextColor(COL_TXT); b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            b.setBackgroundColor(COL_KEY)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
            lp.setMargins(dp(3), dp(4), dp(3), dp(4)); b.layoutParams = lp
            b.setOnClickListener { applyCandidate(cw) }
            bar.addView(b)
        }
    }
    private fun applyCandidate(cw: String) {
        flushComposing()
        val word = wordBuf.toString()
        val c = ic() ?: return
        if (word.isNotEmpty()) c.deleteSurroundingText(word.length, 0)
        c.commitText(cw, 1)
        wordBuf.setLength(0); wordBuf.append(cw)
        updateSuggestions()
    }

    private val COL_BG = Color.parseColor("#0A0F1C")
    private val COL_KEY = Color.parseColor("#1B2440")
    private val COL_FN = Color.parseColor("#161F38")
    private val COL_TXT = Color.parseColor("#E8ECF6")
    private val COL_ACCENT = Color.parseColor("#5FD6B4")

    private fun key(label: String, weight: Float, bg: Int, txtColor: Int, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label; b.isAllCaps = false
        b.setTextColor(txtColor); b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        b.setBackgroundColor(bg); b.setPadding(0, 0, 0, 0)
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
        lp.setMargins(dp(2), dp(2), dp(2), dp(2)); b.layoutParams = lp
        b.setOnClickListener { onClick() }
        return b
    }
    private fun backspaceKey(weight: Float): Button {
        val b = key("⌫", weight, COL_FN, COL_TXT) { }
        b.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    onBackspace()
                    val r = object : Runnable { override fun run() { onBackspace(); repeatHandler.postDelayed(this, 55) } }
                    repeatRunnable = r
                    repeatHandler.postDelayed(r, 400)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    repeatRunnable?.let { repeatHandler.removeCallbacks(it) }; repeatRunnable = null
                    v.performClick(); true
                }
                else -> false
            }
        }
        return b
    }
    private fun row(): LinearLayout {
        val r = LinearLayout(this); r.orientation = LinearLayout.HORIZONTAL
        r.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        return r
    }
    private fun buildKeyboardView(): View {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(COL_BG)
        root.setPadding(dp(4), dp(6), dp(4), dp(8))
        val sb = LinearLayout(this)
        sb.orientation = LinearLayout.HORIZONTAL
        sb.setBackgroundColor(COL_BG)
        sb.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44))
        suggestionBar = sb
        root.addView(sb)
        when (mode) {
            "ko" -> buildKo(root)
            "en" -> buildEn(root)
            else -> buildSym(root)
        }
        return root
    }

    private fun buildKo(root: LinearLayout) {
        val r1 = row()
        for (v in listOf("ㅣ", "ㆍ", "ㅡ")) r1.addView(key(v, 1f, COL_KEY, if (v == "ㆍ") COL_ACCENT else COL_TXT) { onVowel(v) })
        root.addView(r1)
        for (cr in listOf(listOf("ㄱㅋ","ㄴㄹ","ㄷㅌ"), listOf("ㅂㅍ","ㅅㅎ","ㅈㅊ"))) {
            val r = row()
            for (c in cr) r.addView(key(c, 1f, COL_KEY, COL_TXT) { onCons(c) })
            root.addView(r)
        }
        val r4 = row()
        r4.addView(key(". ,", 1f, COL_FN, COL_TXT) { onPunct(".") })
        r4.addView(key("ㅇㅁ", 1f, COL_KEY, COL_TXT) { onCons("ㅇㅁ") })
        r4.addView(backspaceKey(1f))
        root.addView(r4)
        val r5 = row()
        r5.addView(key("!#1", 1f, COL_FN, COL_TXT) { toggleSym() })
        r5.addView(key("한/영", 1f, COL_FN, COL_ACCENT) { toggleLang() })
        r5.addView(key("스페이스", 2.6f, COL_FN, COL_TXT) { onSpace() })
        r5.addView(key("↵", 1f, COL_FN, COL_TXT) { onEnter() })
        root.addView(r5)
    }
    private fun enLetter(ch: String): Button =
        key(if (capsOn) ch.uppercase() else ch, 1f, COL_KEY, COL_TXT) { onEnglish(ch) }
    private fun buildEn(root: LinearLayout) {
        for (line in listOf("qwertyuiop", "asdfghjkl")) {
            val r = row()
            for (ch in line) r.addView(enLetter(ch.toString()))
            root.addView(r)
        }
        val r3 = row()
        r3.addView(key("⇧", 1.5f, COL_FN, if (capsOn) COL_ACCENT else COL_TXT) { capsOn = !capsOn; setInputView(buildKeyboardView()) })
        for (ch in "zxcvbnm") r3.addView(enLetter(ch.toString()))
        r3.addView(backspaceKey(1.5f))
        root.addView(r3)
        val r4 = row()
        r4.addView(key("!#1", 1.2f, COL_FN, COL_TXT) { toggleSym() })
        r4.addView(key("한/영", 1.4f, COL_FN, COL_ACCENT) { toggleLang() })
        r4.addView(key("space", 3.4f, COL_FN, COL_TXT) { onSpace() })
        r4.addView(key(".", 1f, COL_FN, COL_TXT) { onPunct(".") })
        r4.addView(key("↵", 1.4f, COL_FN, COL_TXT) { onEnter() })
        root.addView(r4)
    }
    private fun buildSym(root: LinearLayout) {
        for (line in listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","\$","%","&","-","+","(",")","/"),
            listOf("!","?",".",",",":",";","'","\"","~")
        )) {
            val r = row()
            for (s in line) r.addView(key(s, 1f, COL_KEY, COL_TXT) { onPunct(s) })
            root.addView(r)
        }
        val r4 = row()
        r4.addView(key("가/ABC", 1.6f, COL_FN, COL_ACCENT) { toggleSym() })
        r4.addView(key("space", 3.8f, COL_FN, COL_TXT) { onSpace() })
        r4.addView(backspaceKey(1.4f))
        r4.addView(key("↵", 1.4f, COL_FN, COL_TXT) { onEnter() })
        root.addView(r4)
    }
}
