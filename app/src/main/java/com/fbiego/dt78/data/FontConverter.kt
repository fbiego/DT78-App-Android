/*
 *
 * MIT License
 *
 * Copyright (c) 2021 Felix Biego
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.fbiego.dt78.data

import java.util.*
import kotlin.collections.HashMap

class FontConverter {

    fun greek2English(greek: String): String {

        val sb = StringBuilder()
        val values = HashMap<String, String>()
        val string = greek.toLowerCase(Locale.getDefault())

        values["α"] = "a"
        values["ά"] = "a"
        values["Ά"] = "A"
        values["β"] = "b"
        values["ϐ"] = "b"
        values["γ"] = "g"
        values["Γ"] = "G"
        values["δ"] = "d"
        values["Δ"] = "D"
        values["ε"] = "e"
        values["έ"] = "e"
        values["Έ"] = "E"
        values["ζ"] = "z"
        values["η"] = "i"
        values["ή"] = "i"
        values["Ή"] = "H"
        values["θ"] = "th"
        values["ϴ"] = "TH"
        values["ι"] = "i"
        values["ί"] = "i"
        values["ϊ"] = "i"
        values["κ"] = "k"
        values["λ"] = "l"
        values["Λ"] = "L"
        values["μ"] = "m"
        values["ν"] = "n"
        values["ξ"] = "ks"
        values["Ξ"] = "KS"
        values["ο"] = "o"
        values["ό"] = "o"
        values["π"] = "p"
        values["Π"] = "P"
        values["ρ"] = "r"
        values["σ"] = "s"
        values["ς"] = "s"
        values["Σ"] = "S"
        values["τ"] = "t"
        values["υ"] = "y"
        values["ύ"] = "y"
        values["φ"] = "f"
        values["χ"] = "x"
        values["ψ"] = "ps"
        values["Ψ"] = "PS"
        values["ω"] = "w"
        values["ώ"] = "w"
        values["Ω"] = "W"
        for (i in string.indices) {
            if (values.containsKey((string[i]).toString())) {
                sb.append(values[(string[i]).toString()])
            } else {
                sb.append(string[i])
            }
        }
        return sb.toString()
    }
}