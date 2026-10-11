package com.example.groovemaster.data

object MusicTheoryData {

    val NOTE_NAMES = listOf("C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B")

    /** Cents-aware note name: ↓ = quarter-flat, ↑ = quarter-sharp. */
    fun noteName(cents: Double): String {
        val base = Math.round(cents / 100.0)
        val dev = cents - base * 100.0
        val n = NOTE_NAMES[(((base % 12) + 12) % 12).toInt()]
        return when { dev <= -40 -> "$n↓"; dev >= 40 -> "$n↑"; else -> n }
    }

    data class Scale(val name: String, val family: String, val steps: List<Double>, val info: String)

    val scales = listOf(
        Scale("Major (Ionian)","Western", listOf(0.0,2.0,4.0,5.0,7.0,9.0,11.0), "The bright foundation of Western harmony (W W H W W W H)."),
        Scale("Natural Minor (Aeolian)","Western", listOf(0.0,2.0,3.0,5.0,7.0,8.0,10.0), "The relative minor — dark and melancholic."),
        Scale("Harmonic Minor","Western", listOf(0.0,2.0,3.0,5.0,7.0,8.0,11.0), "Minor with a raised 7th — dramatic, operatic color."),
        Scale("Melodic Minor (asc.)","Western", listOf(0.0,2.0,3.0,5.0,7.0,9.0,11.0), "'Jazz minor' — raised 6th and 7th smooth the ascent."),
        Scale("Dorian Mode","Western", listOf(0.0,2.0,3.0,5.0,7.0,9.0,10.0), "Minor with a bright 6th — jazz, funk and folk favorite."),
        Scale("Phrygian Mode","Western", listOf(0.0,1.0,3.0,5.0,7.0,8.0,10.0), "Spanish-flavored minor — flat 2nd right away."),
        Scale("Lydian Mode","Western", listOf(0.0,2.0,4.0,6.0,7.0,9.0,11.0), "Major with a ♯4 — floating, cinematic brightness."),
        Scale("Mixolydian Mode","Western", listOf(0.0,2.0,4.0,5.0,7.0,9.0,10.0), "Major with a ♭7 — the rock & blues dominant sound."),
        Scale("Locrian Mode","Western", listOf(0.0,1.0,3.0,5.0,6.0,8.0,10.0), "Unstable half-diminished scale — rare and tense."),
        Scale("Blues Scale","Western", listOf(0.0,3.0,5.0,6.0,7.0,10.0), "Minor pentatonic plus the 'blue note' (♭5)."),
        Scale("Major Pentatonic","Western", listOf(0.0,2.0,4.0,7.0,9.0), "Five notes, no wrong ones — folk music worldwide."),
        Scale("Whole Tone","Western", listOf(0.0,2.0,4.0,6.0,8.0,10.0), "All whole steps — Debussy's dream."),
        Scale("Maqam Rast","Middle East", listOf(0.0,2.0,3.5,5.0,7.0,9.0,9.5), "'King of maqams' — 3rd & 7th sit a QUARTER-TONE flat (E↓, B↓ in C Rast). 24-TET approximation."),
        Scale("Maqam Evçara (Rast family)","Middle East", listOf(0.0,2.0,3.5,5.0,7.0,8.5,10.0), "Rast-family maqam with flattened upper jins (E↓, A↓). 24-TET approximation."),
        Scale("Maqam Nahawand","Middle East", listOf(0.0,2.0,3.0,5.0,7.0,8.0,10.0), "Cousin of the natural minor, used across Arabic music."),
        Scale("Maqam Kurd","Middle East", listOf(0.0,1.0,3.0,5.0,7.0,8.0,10.0), "Phrygian-like — flat 2nd; Turkish & Arabic favorite."),
        Scale("Maqam Bayati","Middle East", listOf(0.0,1.0,3.5,5.0,7.0,9.0,10.0), "Warm, soulful; its 3rd is half-flat in practice. D-based."),
        Scale("Maqam Hijaz","Middle East", listOf(0.0,1.0,4.0,5.0,7.0,8.0,10.0), "The famous 'exotic' scale — augmented 2nd between ♭2 and 3."),
        Scale("Raga Bhairav","India", listOf(0.0,1.0,4.0,5.0,7.0,8.0,11.0), "A dawn raga — ♭2 and ♭6 give devotional gravity."),
        Scale("Raga Bhairavi","India", listOf(0.0,1.0,3.0,5.0,7.0,8.0,10.0), "Like Phrygian — the 'om' of Indian melody."),
        Scale("Raga Yaman","India", listOf(0.0,2.0,4.0,6.0,7.0,9.0,11.0), "Evening raga — Lydian-like ♯4, serene and regal."),
        Scale("Raga Bhupali","India", listOf(0.0,2.0,4.0,7.0,9.0), "Major pentatonic — five pure evening notes."),
        Scale("Raga Kafi","India", listOf(0.0,2.0,3.0,5.0,7.0,9.0,10.0), "Dorian-like folk raga — earthy and playful."),
        Scale("Raga Malkauns","India", listOf(0.0,3.0,5.0,8.0,10.0), "Haunted pentatonic night raga (komal ga, dha, ni)."),
        Scale("Yo Scale (Japan)","Japan", listOf(0.0,2.0,5.0,7.0,9.0), "Japanese pentatonic with no semitones — clear folk sound."),
        Scale("In-Sen Scale (Japan)","Japan", listOf(0.0,1.0,5.0,7.0,10.0), "Semitone + fourth — the dark koto/shamisen color."),
        Scale("Gong Scale (China)","China", listOf(0.0,2.0,4.0,7.0,9.0), "Chinese wǔshēng pentatonic — the classic 'oriental major'."),
        Scale("Hungarian Minor","Eastern Europe", listOf(0.0,2.0,3.0,6.0,7.0,8.0,11.0), "'Gypsy minor' — two augmented 2nds, fiery and dark."),
        Scale("Byzantine (Double Harmonic)","Greece", listOf(0.0,1.0,4.0,5.0,7.0,8.0,11.0), "Two augmented 2nds — the Eastern Mediterranean sound."),
        Scale("Echos Protos (Byzantine 1st)","Byzantine", listOf(0.0,2.0,3.0,5.0,7.0,8.0,10.0), "1st mode of Byzantine chant (diatonic form) — kin to the Aeolian."),
        Scale("Echos Deuteros (Byzantine 2nd)","Byzantine", listOf(0.0,1.0,4.0,5.0,7.0,8.0,10.0), "2nd mode — the Hijaz-type augmented 2nd rings through Orthodox chant."),
        Scale("Echos Tetartos (Byzantine 4th)","Byzantine", listOf(0.0,1.0,3.0,5.0,7.0,8.0,10.0), "4th mode — Phrygian/Kurd-type color in Greek liturgy."),
        Scale("Freygish / Ahava Rabbah","Klezmer", listOf(0.0,1.0,4.0,5.0,7.0,8.0,10.0), "The klezmer 'Jewish mode' — the SAME scale as Maqam Hijaz. One scale, many homes."),
        Scale("Mi Sheberach","Klezmer", listOf(0.0,2.0,3.0,6.0,7.0,9.0,10.0), "Klezmer 'healing prayer' mode — the ♯4 gives its bittersweet lift."),
        Scale("Dastgah-e Shur","Persia", listOf(0.0,1.0,3.0,5.0,6.0,8.0,10.0), "Iran's most-beloved dastgāh — ♭2 ♭3 ♭6 shading (approximation)."),
        Scale("Dastgah-e Homayun","Persia", listOf(0.0,1.0,4.0,5.0,7.0,8.0,11.0), "Majestic dastgāh, the 'celestial' partner-world of Shur (approximation)."),
        Scale("Dastgah-e Mahur","Persia", listOf(0.0,2.0,4.0,5.0,7.0,9.0,11.0), "≈ the Western major scale — but phrased with Persian gusheh logic."),
        Scale("Dastgah-e Segah","Persia", listOf(0.0,1.0,4.0,5.0,7.0,9.0,10.0), "Centers on a (half-flat) 3rd — approximated with the augmented 2nd."),
        Scale("Protus authentic (Gregorian Mode I)","Gregorian", listOf(0.0,2.0,3.0,5.0,7.0,9.0,10.0), "Mode 1 on D — the chant 'Dorian'. Final D, reciting tone A."),
        Scale("Deuterus authentic (Gregorian Mode III)","Gregorian", listOf(0.0,1.0,3.0,5.0,7.0,8.0,10.0), "Mode 3 on E — the chant 'Phrygian'."),
        Scale("Tritus authentic (Gregorian Mode V)","Gregorian", listOf(0.0,2.0,4.0,6.0,7.0,9.0,11.0), "Mode 5 on F — the chant 'Lydian'. Medieval singers flattened B to avoid the tritone."),
        Scale("Tetrardus authentic (Gregorian Mode VII)","Gregorian", listOf(0.0,2.0,4.0,5.0,7.0,9.0,10.0), "Mode 7 on G — the chant 'Mixolydian'.")
    )

    data class ChordType(val name: String, val steps: List<Int>, val info: String)

    val chordTypes = listOf(
        ChordType("Major", listOf(0,4,7), "Root + major 3rd + perfect 5th — bright and stable."),
        ChordType("Minor", listOf(0,3,7), "Root + minor 3rd + perfect 5th — dark and stable."),
        ChordType("Diminished", listOf(0,3,6), "Stacked minor 3rds — tense, wants to move."),
        ChordType("Augmented", listOf(0,4,8), "Stacked major 3rds — eerie, floating."),
        ChordType("Suspended 4th", listOf(0,5,7), "3rd replaced by a 4th — open, unresolved."),
        ChordType("Dominant 7", listOf(0,4,7,10), "Major triad + ♭7 — the engine of blues and V→I motion."),
        ChordType("Major 7", listOf(0,4,7,11), "Major triad + major 7 — lush, jazzy, dreamy."),
        ChordType("Minor 7", listOf(0,3,7,10), "Minor triad + ♭7 — smooth, soulful."),
        ChordType("Half-diminished m7♭5", listOf(0,3,6,10), "The ii chord of minor keys — noir tension."),
        ChordType("Diminished 7", listOf(0,3,6,9), "Fully symmetric — suspense and surprise.")
    )

    data class Interval(val semitones: Int, val name: String)

    val intervals = listOf(
        Interval(1,"Minor 2nd"), Interval(2,"Major 2nd"), Interval(3,"Minor 3rd"),
        Interval(4,"Major 3rd"), Interval(5,"Perfect 4th"), Interval(6,"Tritone"),
        Interval(7,"Perfect 5th"), Interval(8,"Minor 6th"), Interval(9,"Major 6th"),
        Interval(10,"Minor 7th"), Interval(11,"Major 7th"), Interval(12,"Octave")
    )

    data class Raga(val name: String, val semitones: List<Int>, val sargam: String,
                    val vadi: String, val samvadi: String, val time: String,
                    val pakad: String, val info: String)

    val ragas = listOf(
        Raga("Bhairav", listOf(0,1,4,5,7,8,11), "S r G m P d N", "Dha", "Re", "Dawn",
            "G m r S n d P", "Solemn dawn raga: komal Re & Dha with shuddha Ga & Ni."),
        Raga("Bhairavi", listOf(0,1,3,5,7,8,10), "S r g m P d n", "Dha", "Ga", "Morning · the traditional closing raga",
            "g m P d m g r S", "All komal re ga dha ni — often ends concerts."),
        Raga("Yaman", listOf(0,2,4,6,7,9,11), "S R G M′ P D N", "Ga", "Ni", "Evening",
            "N R G M′ P M′ G", "Regal, with tivra Ma; the first 'serious' raga of many students."),
        Raga("Bhupali", listOf(0,2,4,7,9), "S R G P D", "Ga", "Dha", "Evening",
            "G P D P G R S", "Major-pentatonic, pure and open."),
        Raga("Malkauns", listOf(0,3,5,8,10), "S g M d n", "Ma", "Sa", "Late night",
            "m d n d P m g", "Haunted pentatonic; vadi–samvadi Ma–Sa anchor its stillness."),
        Raga("Durga", listOf(0,2,5,7,9), "S R M P D", "Sa", "Pa", "Evening",
            "P D P m R S", "Bright evening pentatonic — no Ga, no Ni."),
        Raga("Khamaj", listOf(0,2,4,5,7,9,10), "S R G m P D n", "Ga", "Ni(komal)", "Night",
            "P D n D P m G R", "Major with a komal Ni sliding in on descent — thumri favorite."),
        Raga("Kedar", listOf(0,2,4,5,7,11), "S R G m P N", "Sa", "Pa", "Night",
            "S m G m P m G R S", "Hexatonic; shuddha & tivra Ma trade places in its signature turn."),
        Raga("Bageshri", listOf(0,2,3,5,7,8,10), "S R g m P d n", "Ma", "Sa", "Night",
            "M d n d P M g R S", "Yearning night raga around dha–ni; beloved in thumri."),
        Raga("Desh", listOf(0,2,4,5,7,9,10), "S R M P D n", "Re", "Pa", "Evening",
            "N S R M P m G R n S", "Komala Ga flashes in descent; 'Vande Mataram' lives here."),
        Raga("Charukesi", listOf(0,2,3,5,7,8,11), "S R g m P d N", "Re", "Pa", "Late night",
            "S R P M G M R S", "Carnatic import with a bittersweet ♭3–♭6; film composers love it."),
        Raga("Todi (Miyan ki Todi)", listOf(0,1,3,6,7,8,11), "S r g M′ P d N", "Dha", "Ga", "Morning",
            "d N r G M′ g r S", "Morning court raga: komal r g d with tivra Ma — poise and tension."),
        Raga("Marwa", listOf(0,1,4,6,9,11), "S r G M′ d N", "Re", "Dha", "Sunset",
            "r G M′ d N d M′ G r S", "No Pa at all — the lonely twilight gap is the point."),
        Raga("Puriya", listOf(0,1,4,6,9,11), "S r G M′ d N", "Ga", "Ni", "Sunset",
            "N r G M′ G r S", "Same notes as Marwa, different gravity: Ga-centered."),
        Raga("Purvi", listOf(0,1,4,6,7,8,11), "S r G M′ P d N", "Ga", "Ni", "Dusk",
            "r G M′ P d N d M′ G r S", "Tivra Ma AND Pa — grand, ceremonial twilight."),
        Raga("Hamsadhwani", listOf(0,2,4,7,11), "S R G P N", "Ga", "Pa", "Evening",
            "G P N S' N P G R S", "'Swan's call' — auspicious pentatonic that opens concerts."),
        Raga("Tilak Kamod", listOf(0,2,4,5,7,10), "S R G m P n", "Re", "Pa", "Night",
            "n P m R S", "Charming, lilting; its phrase starts on komal Ni."),
        Raga("Brindabani Sarang", listOf(0,2,5,7,10), "S R m P n", "Re", "Pa", "Midday",
            "R m P n P m R S", "Soothing midday pentatonic with komal Ni."),
        Raga("Jaijaivanti", listOf(0,2,4,5,7,8,10,11), "S R G m P d n N", "Re", "Pa", "Late night",
            "g m P d N d P m g R S", "Both Ni shades — regal, ornate, demanding."),
        Raga("Shivaranjani", listOf(0,2,3,7,9), "S R g P D", "Pa", "Sa", "Evening",
            "G P D P g R S", "Major pentatonic with a minor 3rd — instantly wistful.")
    )

    /** Parse a pakad like "M d n d P M g R S" into cents from Sa. ' or ′ after S = octave. */
    fun pakadCents(pakad: String, base: Double = 0.0): List<Double> {
        val map = mapOf('S' to 0,'r' to 1,'R' to 2,'g' to 3,'G' to 4,'m' to 5,'M' to 6,
                        'P' to 7,'d' to 8,'D' to 9,'n' to 10,'N' to 11)
        val out = mutableListOf<Double>()
        val cs = pakad.toCharArray(); var i = 0
        while (i < cs.size) {
            val ch = cs[i]
            if (ch == ' ' || ch == '-' || ch == '′' || ch == '\'') { i++; continue }
            map[ch]?.let {
                var v = it
                if (ch == 'S' && i + 1 < cs.size && (cs[i + 1] == '\'' || cs[i + 1] == '′')) v = 12
                out.add(base + v * 100.0)
            }
            i++
        }
        return out
    }

    data class Layer(val label: String, val inst: String, val pattern: String)

    data class Groove(
        val name: String, val origin: String, val family: String, val bpm: Int,
        val meter: String, val swing: Float = 0f, val info: String,
        val layers: List<Layer>, val markers: List<Layer> = emptyList()
    ) { val pulses: Int get() = layers.maxOf { it.pattern.length } }

    val grooves = listOf(
        // ---- Americas ----
        Groove("Backbeat Rock","USA","Americas",100,"4/4",0f,"Snare on 2 & 4 — the heartbeat of popular music.",
            listOf(Layer("Kick","kick","x.......x......." ),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("New Orleans Funk","USA","Americas",96,"4/4",0f,"Syncopated kick + snare pops — the Meters' pocket.",
            listOf(Layer("Kick","kick","x.....x...x....."),Layer("Snare","snare","....x.......x.x."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Reggae One Drop","Jamaica","Americas",76,"4/4",0f,"Kick & snare land together on beat 3 only.",
            listOf(Layer("Kick","kick","........x......."),Layer("Snare","snare","........x......."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Merengue","Dominican Rep.","Americas",120,"4/4",0f,"Doubled tresillo tambora — unstoppable dance lilt.",
            listOf(Layer("Tambora","kick","x..x..x.x..x..x."),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Huayno","Peru–Bolivia (Andes)","Americas",150,"2/4",0f,"Andean gallop: booming bass on 1, whip-crack snare on 2.",
            listOf(Layer("Bass","kick","x...x..."),Layer("Snare","snare","....x..."),Layer("Hi-hat","hat","x.x.x.x."))),
        // ---- Latin ----
        Groove("Son Clave 3-2","Cuba","Latin (Cuba/Brazil)",90,"4/4",0f,"The foundational Cuban key pattern (the 'Bo Diddley' cell).",
            listOf(Layer("Clave","wood","x..x..x...x..x.."),Layer("Bass","kick","x.......x......." ),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Rumba Clave 3-2","Cuba","Latin (Cuba/Brazil)",90,"4/4",0f,"Like son clave, but the 3rd stroke leans one sixteenth later.",
            listOf(Layer("Clave","wood","x..x...x..x..x.."),Layer("Bass","kick","x.......x......." ),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Habanera / Tresillo","Cuba → World","Latin (Cuba/Brazil)",96,"4/4",0f,"The 3+3+2 cell — from Havana to Ravel's Boléro to 'Thriller'.",
            listOf(Layer("Bass","kick","x.....x.x......." ),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Bossa Nova","Brazil","Latin (Cuba/Brazil)",80,"4/4",0f,"Soft clave + bossa bass anticipating beats 2 & 4.",
            listOf(Layer("Bass","kick","x.....x.x.....x."),Layer("Rim","snare","x..x..x...x...x."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Samba","Brazil","Latin (Cuba/Brazil)",110,"2/4",0f,"Surdo pushes 2 & 4 with pickups — carnival energy.",
            listOf(Layer("Surdo","kick","....x.x.....x.x."),Layer("Tamborim","wood","x.x.x.x.x.x.x.x."),Layer("Hat","hat","..x...x...x...x."))),
        Groove("Baiao (Forró)","NE Brazil","Latin (Cuba/Brazil)",110,"2/4",0f,"Zabumba plays 3+3+2 across two bars; triangle buzzes above. Gonzaga made it the sound of the sertão.",
            listOf(Layer("Zabumba","kick","x..x..x."),Layer("Caixa","snare","..x.x.x."),Layer("Triangle","hat","x.x.x.x."))),
        // ---- Middle East ----
        Groove("Maqsoum","Egypt","Middle East",100,"4/4",0f,"THE belly-dance groove: DUM … tek … tek DUM … tek.",
            listOf(Layer("Dum","kick","x.......x......." ),Layer("Tek","snare","..x...x.....x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Malfouf","Levant","Middle East",105,"2/4",0f,"Light running iqa': Dum . tek . . tek . — the FAST one.",
            listOf(Layer("Dum","kick","x......."),Layer("Tek","snare","...x..x."),Layer("Hat","hat","x.x.x.x."))),
        Groove("Ayyub","Egypt","Middle East",100,"2/4",0f,"Trance zar rhythm: DUM . . DUM . . TEK — hypnotic 'boom boom-bap'.",
            listOf(Layer("Dum","kick","x..x..."),Layer("Tek","snare","......x."),Layer("Hat","hat","x.x.x.x."))),
        Groove("Baladi (Masmudi Saghir)","Egypt","Middle East",90,"4/4",0f,"Heavy DUM DUM tek DUM tek — earthier cousin of maqsoum.",
            listOf(Layer("Dum","kick","xx..x..."),Layer("Tek","snare","...x..x."),Layer("Hat","hat","x.x.x.x."))),
        Groove("Saidi","Upper Egypt","Middle East",95,"4/4",0f,"DUM tek . DUM DUM tek — the rhythm of the mizmar & stick dance.",
            listOf(Layer("Dum","kick","x...xx.."),Layer("Tek","snare","..x...x."),Layer("Hat","hat","x.x.x.x."))),
        Groove("Samai Thaqil","Ottoman–Egypt","Middle East",60,"10/8",0f,"Stately 10/8 iqa' underpinning the classical samai form.",
            listOf(Layer("Dum","kick","x....xx..."),Layer("Tek","snare","...x...x.."),Layer("Hat","hat","x.x.x.x.x."))),
        Groove("Düyek","Turkey","Middle East",100,"8/8 usul",0f,"Ottoman classical usul: DUM . tek ke · DUM . tek ke.",
            listOf(Layer("Dum","kick","x...x..."),Layer("Tek","snare","..x.x..."),Layer("Hat","hat",".x.x.x.x"))),
        Groove("Aksak Semai","Turkey","Middle East",110,"9/8 (2+2+2+3)",0f,"Same 9 as karsilama, different accent weight — the long beat leans where the melody breathes.",
            listOf(Layer("Accents","wood","X.X.X..X."),Layer("Bass","kick","x.x.x..x."),Layer("Tek","snare","...x...x."),Layer("Hat","hat",".x.x.x.x."))),
        // ---- Balkans & Greece ----
        Groove("Kalamatianos","Greece","Balkans & Greece",130,"7/8 (2+2+3)",0f,"Greek dance 7/8: two quick pairs then the long beat.",
            listOf(Layer("Accents","wood","X.X.X.."),Layer("Bass","kick","x.x.x.."),Layer("Hat","hat",".x.x.x."))),
        Groove("Karsilama","Turkey/Balkans","Balkans & Greece",110,"9/8 (2+2+2+3)",0f,"9-beat belly-dance aksak: 2+2+2+3.",
            listOf(Layer("Accents","wood","X.X.X.X.."),Layer("Bass","kick","x.x.x.x.."),Layer("Hat","hat",".x.x.x.x."))),
        Groove("Paidushko","Bulgaria","Balkans & Greece",140,"5/8 (2+3)",0f,"Bulgarian 5/8 — limping 'short-long' dance meter.",
            listOf(Layer("Accents","wood","X.X.."),Layer("Bass","kick","x.x.."),Layer("Hat","hat","x.x.x"))),
        Groove("Aksak 11/16","Balkans","Balkans & Greece",120,"11/16 (2+2+2+2+3)",0f,"Eleven sixteenths: four pairs chasing a long beat.",
            listOf(Layer("Accents","wood","X.X.X.X.X.."),Layer("Bass","kick","x.x.x.x.x.."),Layer("Hat","hat","x.x.x.x.x.x"))),
        Groove("Hasapiko","Greece","Balkans & Greece",110,"4/4",0f,"'Butcher's dance' — steady, grounded 4s; Zorba's slow build.",
            listOf(Layer("Kick","kick","x...x...x...x..."),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Syrtos","Greece (islands)","Balkans & Greece",90,"4/4",0f,"Chain-dance 4/4 gliding on a tresillo bass.",
            listOf(Layer("Bass","kick","x.....x.x......." ),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Zeibekiko","Greece","Balkans & Greece",70,"9/8 (2+2+2+3)",0f,"Slow, heavy 9 for one dancer alone — arms wide, floor stared down.",
            listOf(Layer("Accents","wood","X.X.X.X.."),Layer("Bass","kick","x.x.x.x.."),Layer("Tek","snare","......x.."))),
        // ---- India: talas ----
        Groove("Teentaal","India","India",90,"16 matras (4+4+4+4)",0f,
            "King of talas. Theka: Dha Dhin Dhin Dha | Dha Dhin Dhin Dha | Dha Tin Tin Ta | Ta Dhin Dhin Dha. Claps (taali) on 1, 5, 13; WAVE (khaali) on 9.",
            listOf(Layer("Bayan (bass)","kick","x..xx..x.......x"),Layer("Dayan (treble)","snare",".xx.xx..xxxxxx..")),
            listOf(Layer("Taali (clap)","clap","x...x...x...x..."),Layer("Khaali (wave)","wave",".......x........"))),
        Groove("Keherwa","India","India",120,"8 matras (4+4)",0f,
            "THE Bollywood/ghazal groove. Theka: Dha Ge Na Ti | Na Ka Dha Ge. Taali on 1, khaali on 5.",
            listOf(Layer("Bass","kick","xx....xx"),Layer("Treble","snare","..xxxx..")),
            listOf(Layer("Taali","clap","x......."),Layer("Khaali","wave","....x..."))),
        Groove("Dadra","India","India",130,"6 matras (3+3)",0f,"Light 6-beat cycle used in thumri & bhajan. Taali on 1, khaali on 4.",
            listOf(Layer("Bass","kick","xx.x.."),Layer("Treble","snare","..x.xx")),
            listOf(Layer("Taali","clap","x....."),Layer("Khaali","wave","...x.."))),
        Groove("Rupak","India","India",100,"7 matras (3+2+2)",0f,
            "The odd one: Rupak STARTS with khaali (wave) — no clap on sam! Taali on 4 and 6.",
            listOf(Layer("Bass","kick","...x.x."),Layer("Treble","snare","xxx.x.x")),
            listOf(Layer("Khaali","wave","x......"),Layer("Taali","clap","...x.x."))),
        Groove("Jhaptaal","India","India",110,"10 matras (2+3+2+3)",0f,
            "Ten-beat cycle: taali on 1, 5, 8; khaali on 6.",
            listOf(Layer("Bass","kick","x..x.x...x"),Layer("Treble","snare",".xx.x.x.xx")),
            listOf(Layer("Taali","clap","x...x..x.."),Layer("Khaali","wave",".....x...."))),
        Groove("Ektaal","India","India",84,"12 matras (2+2+2+2+2+2)",0f,
            "The classical 12: taali on 1, 5, 7; khaali (wave) on 11. Kaida and tukra territory.",
            listOf(Layer("Bayan","kick","x..x..x..x.."),Layer("Dayan","snare",".xx.xx..xx.x")),
            listOf(Layer("Taali","clap","x...x.x....."),Layer("Khaali","wave","..........x."))),
        Groove("Dhamar","India","India",62,"14 matras (5+2+3+4)",0f,
            "Stately 14 with khaali on 8 — the cycle of dhrupad and holi songs.",
            listOf(Layer("Bass","kick","x..x.....x..x."),Layer("Treble","snare","..x.x..x.x..x.")),
            listOf(Layer("Taali","clap","x............."),Layer("Khaali","wave",".......x......"))),
        Groove("Deepchandi","India","India",92,"14 matras (3+4+3+4)",0f,
            "Swinging 14: taali on 1, 4, 6; wave on 11 — thumri's favorite cradle.",
            listOf(Layer("Bass","kick","x..x...x..x..."),Layer("Treble","snare","..xx...x..xx..")),
            listOf(Layer("Taali","clap","x..x.x........"),Layer("Khaali","wave","..........x..."))),
        Groove("Bhangra","Punjab","India",105,"4/4",0f,"Dhol-driven: heavy dum + hopping pickup at the bar's end.",
            listOf(Layer("Dhol","kick","x.......x.....x."),Layer("Snare","snare","....x.......x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        // ---- Africa ----
        Groove("Gankogui 'Standard Pattern'","Ewe, Ghana","Africa",100,"12 pulses (12/8)",0f,
            "THE West-African bell timeline (hits at 1, 4, 6, 10, 12). Every Ewe drum ensemble hangs off it.",
            listOf(Layer("Gankogui (bell)","bell","x..x..x...x."),Layer("Low bell","metal","x.......x..."),Layer("Axatse","hat","x.x.x.x.x.x."))),
        Groove("Gahu Bell","Ghana–Benin","Africa",100,"12 pulses",0f,"Social-music bell line — a rotated cousin of the standard pattern.",
            listOf(Layer("Bell","bell","x..x..x.x.x."),Layer("Bass","kick","x.....x.x..."),Layer("Shaker","hat","x.x.x.x.x.x."))),
        Groove("Bembé Bell","Afro-Cuban / W. Africa","Africa",96,"12 pulses",0f,"7-stroke 12/8 bell — the sacred bembé timeline.",
            listOf(Layer("Bell","bell","x.xx.xx.x.x."),Layer("Conga","kick","x.....x.x..."),Layer("Hat","hat","x.x.x.x.x.x."))),
        Groove("Hocket Chant","West Africa","Africa",110,"12 pulses",0f,"HOCKETING: two parts split one melody — together they fill every pulse.",
            listOf(Layer("Part A","bell","x.x.x.x.x.x."),Layer("Part B","metal",".x.x.x.x.x.x"),Layer("Clap","clap","x.......x..."))),
        Groove("Afrobeat","Nigeria (Fela)","Africa",104,"4/4",0f,"Interlocking guitars + baritone over sharp horn punches; drums keep a rolling 4 with ghosts.",
            listOf(Layer("Kick","kick","x...x..x..x.x..."),Layer("Snare","snare","....x..x....x..x"),Layer("Guitar/hat","hat","x.xxx.xxx.x.xxx."))),
        Groove("Highlife","Ghana","Africa",104,"12/8",0f,"Palm-wine guitar sparkle over a 12-pulse bell — the grandfather of afrobeat.",
            listOf(Layer("Bell","wood","x..x..x.x.x."),Layer("Bass","kick","x.....x.x..."),Layer("Hat","hat","x.x.x.x.x.x."))),
        Groove("Coupé-Décalé","Côte d'Ivoire","Africa",102,"4/4",0f,"Abidjan club sound: four-on-floor kick with a lurching snare cell.",
            listOf(Layer("Kick","kick","x...x...x...x..."),Layer("Snare","snare","..x.x...x.x.x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Soukous (Sebene)","Congo","Africa",124,"4/4",0f,"The sebene: dizzy interlocked guitar figures cycling over a steady pulse.",
            listOf(Layer("Kick","kick","x...x...x...x..."),Layer("Guitars","wood","x..xx..xx..xx..x"),Layer("Bell","bell","x.x.x.x.x.x.x.x."))),
        // ---- East & SE Asia ----
        Groove("Matsuri Taiko","Japan","East & SE Asia",95,"4/4",0f,"'Don don ko' — festival drum call-and-answer.",
            listOf(Layer("Ōdaiko","kick","x...x.x.x...x.x."),Layer("Chūdaiko","tom","..x.......x....."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Kotekan","Bali, Indonesia","East & SE Asia",120,"4/4",0f,
            "Balinese interlocking hocket: polos + sangsih weave ONE fast line around the slow pokok melody.",
            listOf(Layer("Polos","bell","x.x.x.x.x.x.x.x."),Layer("Sangsih","metal",".x.x.x.x.x.x.x.x"),Layer("Pokok (gong)","tom","x.......x......." ))),
        Groove("Samulnori Uttdari","Korea","East & SE Asia",90,"12/8",0f,
            "Pungmul farmers' band: janggu (low+high), jing gong, kkwaenggwari — rolling 3+3+3+3.",
            listOf(Layer("Janggu low","kick","x..x..x..x.."),Layer("Janggu high","snare","x.x.x.x.x.x."),Layer("Jing","bell","x.....x....."),Layer("Kkwaenggwari","wood","....x.....x."))),
        Groove("Gong Kebyar skeleton","Bali","East & SE Asia",108,"4/4",0f,"Kajar marks the beat; gong & kendang mark the cycle's turn.",
            listOf(Layer("Kajar","hat","x.x.x.x.x.x.x.x."),Layer("Gong","tom","x.......x......." ),Layer("Kendang","kick","x..x..x.x..x..x."))),
        // ---- Spain (Flamenco) ----
        Groove("Soleá","Spain","Spain (Flamenco)",75,"12-count compás",0f,
            "The mother palo: 12 counts with accents on 3, 6, 8, 10 & 12 — the 'expected' stresses are displaced.",
            listOf(Layer("Palmas","clap","..X..X.X.X.X"),Layer("Cajón","kick","x..x..x.x.x."))),
        Groove("Bulerías","Spain","Spain (Flamenco)",130,"12-count compás",0f,
            "Fast, playful 12 — the '1' often lands on the LAST count. Jerez parties end with these.",
            listOf(Layer("Palmas","clap","X.X..X.X.X.."),Layer("Cajón","kick","x.x..x.x.x.."))),
        Groove("Alegrías","Spain","Spain (Flamenco)",110,"12-count compás",0f,
            "Cádiz joy: accents on 12, 3, 5, 8 & 10 — lighter than soleá.",
            listOf(Layer("Palmas","clap","X.X.X..X.X.."),Layer("Cajón","kick","x.x.x..x.x.."))),
        Groove("Tango Flamenco","Spain","Spain (Flamenco)",120,"4/4",0f,
            "Party-style 4-count with sharp pick-up accents before each beat.",
            listOf(Layer("Palmas","clap","X.x.X.x.X.x.X.x."),Layer("Cajón","kick","x...x...x...x..."))),
        // ---- Urban & Electronic ----
        Groove("Dembow","PR → Global","Urban & Electronic",96,"4/4",0f,
            "The reggaeton engine: boom … ch-boom-chick. Once you hear it you can't unhear it.",
            listOf(Layer("Kick","kick","x...x...x...x..."),Layer("Snare","snare","..x.x.....x.x..."),Layer("Hat","hat","x.x.x.x.x.x.x.x."))),
        Groove("Drum'n'bass","UK","Urban & Electronic",172,"4/4",0f,
            "174-ish BPM breakbeat: snare on 2 & 4 with a ghost 'o' 16th; sub-kick at 1 and 11.",
            listOf(Layer("Kick","kick","x.........x....."),Layer("Snare","snare","....x.......x.o."),Layer("Hats+ghosts","hat","x.xxx.x.xxx.x.xo"))),
        Groove("Trap","USA","Urban & Electronic",140,"4/4 (half-time)",0f,
            "Half-time snare at 3, triplet-flavored hat rolls — 'o' marks the quiet ghost hits.",
            listOf(Layer("808","kick","x.....x...x.x..."),Layer("Snare","snare","........x......." ),Layer("Hat roll","hat","x.oxx.oxx.xoxxx.")))
    )

    data class QuizQ(val q: String, val options: List<String>, val answer: Int, val info: String)

    val theoryQuestions = listOf(
        QuizQ("How many notes in a chromatic scale?", listOf("7","12","5","8"), 1, "12 semitones per octave."),
        QuizQ("Which interval is nicknamed 'the devil's interval'?", listOf("Minor 3rd","Tritone","Perfect 5th","Major 6th"), 1, "The tritone (6 semitones) — 'diabolus in musica'."),
        QuizQ("'DUM' and 'TEK' are the two basic strokes of which drum?", listOf("Taiko","Darbuka","Conga","Snare"), 1, "The Middle-Eastern darbuka (goblet drum)."),
        QuizQ("Which raga is traditionally performed at dawn?", listOf("Raga Yaman","Raga Bhairav","Raga Bhupali","Raga Kafi"), 1, "Bhairav is a morning sandhi-prakash raga."),
        QuizQ("A minor 7th chord stacks which intervals from the root?", listOf("M3+m3+M3","m3+m3+m3","m3+M3+m3","M3+M3+M3"), 2, "m3+M3 = minor triad; add a m3 = minor 7."),
        QuizQ("The oud — ancestor of the lute and guitar — comes from…", listOf("India","Japan","The Arab world","Peru"), 2, "It traveled via Al-Andalus into Europe."),
        QuizQ("'Tala' in Indian music means…", listOf("Melodic ornament","Rhythmic cycle","Drone instrument","Scale"), 1, "e.g. Teentaal is a 16-beat cycle."),
        QuizQ("How many beats in a bar of waltz time?", listOf("2","3","4","6"), 1, "ONE-two-three…"),
        QuizQ("A perfect fifth above C is…", listOf("F","G","A","E"), 1, "C→G = 7 semitones."),
        QuizQ("Which mode equals a major scale from its 6th degree?", listOf("Dorian","Phrygian","Aeolian","Lydian"), 2, "Aeolian = natural minor."),
        QuizQ("Klezmer music often uses which exotic-sounding scale?", listOf("Whole tone","Hungarian minor","Major pentatonic","Octatonic"), 1, "Hungarian (Gypsy) minor, with its augmented 2nds."),
        QuizQ("The steelpan comes from…", listOf("Hawaii","Trinidad & Tobago","Bali","Ireland"), 1, "Invented in Trinidad from oil drums."),
        QuizQ("Which is NOT a mode of the major scale?", listOf("Dorian","Hijaz","Mixolydian","Lydian"), 1, "Hijaz belongs to the maqam system."),
        QuizQ("In 'son clave 3-2', the 3 refers to…", listOf("3 beats","The 3-stroke side of the pattern","3 bars","Volume level"), 1, "A 3-side and a 2-side; their order gives 3-2 or 2-3."),
        QuizQ("Which scale uses only whole steps?", listOf("Whole tone","Harmonic minor","Blues","Dorian"), 0, "Six notes, each a whole step apart — fully symmetric."),
        QuizQ("Bossa nova fuses samba rhythm with which harmony?", listOf("Punk","Jazz","Metal","Folk"), 1, "Jazz 7th and 9th chords over a samba pulse."),
        QuizQ("A gamelan ensemble comes from…", listOf("Indonesia (Java/Bali)","Ghana","Japan","Norway"), 0, "Bronze gongs and metallophones."),
        QuizQ("How many semitones are in a minor 3rd?", listOf("2","3","4","5"), 1, "A major 3rd is 4 semitones."),
        QuizQ("Taiko drumming ensembles come from…", listOf("Korea","Japan","China","Mongolia"), 1, "'Taiko' means 'great drum' in Japanese."),
        QuizQ("Reggae's one-drop puts kick & snare on which beat?", listOf("1","2","3","4"), 2, "Both land together on beat 3 only."),
        QuizQ("The Byzantine / Double Harmonic scale evokes…", listOf("French impressionism","Eastern Mediterranean chant","Grunge","Baroque counterpoint"), 1, "Its two augmented 2nds ring 'Eastern'."),
        QuizQ("How many shrutis does Indian theory define in an octave?", listOf("22","12","7","36"), 0, "22 — 'shruti' = 'that which is heard'."),
        QuizQ("The PURE (just) perfect fifth is how many cents above the tonic?", listOf("700","702","680","720"), 1, "702 cents (ratio 3:2); ET flattens it to 700."),
        QuizQ("In Maqam Rast, which degrees are quarter-flat?", listOf("3rd and 7th","2nd and 6th","1st and 5th","4th only"), 0, "E↓ and B↓ in C Rast (~350¢ and ~950¢)."),
        QuizQ("Slendro (Java/Bali) divides the octave into…", listOf("5 nearly-equal tones","7 uneven tones","12 equal tones","4 tones"), 0, "≈240¢ steps — and no two ensembles agree exactly!"),
        QuizQ("Pelog theoretically has how many tones (usually 5 used)?", listOf("7","5","9","12"), 0, "Uneven 7-tone tuning; a pathet selects ~5."),
        QuizQ("'Kotekan', the interlocking Balinese figuration, is a form of…", listOf("Hocketing","Drone","Glissando","Solo ostinato"), 0, "Polos + sangsih interlock into one fast line."),
        QuizQ("Freygish (Ahava Rabbah) is the same scale as…", listOf("Maqam Hijaz","Raga Yaman","Dorian","Whole tone"), 0, "One scale — Jewish, Arab and flamenco homes."),
        QuizQ("A 'dastgāh' is a…", listOf("Persian melodic system","Korean drum","Greek mode","African bell"), 0, "Iran's modal system: Shur, Homayun, Mahur, Segah…"),
        QuizQ("'Samai Thaqil' is an iqa' (rhythmic cycle) of how many beats?", listOf("10","16","7","4"), 0, "10/8 — the stately frame of the classical samai."),
        QuizQ("The VADI of a raga is…", listOf("Its most important ('king') note","The drone","The drummer","The fastest note"), 0, "The melody dwells on it constantly."),
        QuizQ("The SAMVADI is…", listOf("The 2nd-most important note, usually a 4th/5th from the vadi","The lowest note","The final note","A drum stroke"), 0, "e.g. Ga–Ni in Yaman, Ma–Sa in Malkauns."),
        QuizQ("Raga Malkauns is traditionally sung…", listOf("Late at night","At dawn","At noon","During harvest"), 0, "A haunted pentatonic night raga."),
        QuizQ("In sargam, lowercase letters (r g d n) mean…", listOf("Komal (flatter) notes","Quiet notes","Low octave","Fast notes"), 0, "Uppercase = shuddha; M′ = tivra (sharp) Ma."),
        QuizQ("'Taali' and 'khaali' in a tala mean…", listOf("Clap and wave (empty) beats","Fast and slow","Loud and soft","Start and end"), 0, "Rupak famously begins on khaali!"),
        QuizQ("Soleá compás accents fall on counts…", listOf("3, 6, 8, 10, 12","1, 2, 3, 4","2 and 4","1 and 5"), 0, "Displaced stress = the flamenco feel."),
        QuizQ("Rondo form looks like…", listOf("A B A C A","A A B A","A B","A A A"), 0, "Keep returning 'home' to A between episodes."),
        QuizQ("'Ternary form' means…", listOf("A B A","A B","A A B","A B C A"), 0, "Statement – contrast – return."),
        QuizQ("AABA is also known as…", listOf("32-bar song form","12-bar blues","Rondo","Strophic"), 0, "The classic jazz standard form."),
        QuizQ("The 12-bar blues mainly uses which chords?", listOf("I, IV, V","ii, V, I","I, vi, IV","vi, ii, V"), 0, "Four bars I, two IV, back to I, then V–IV–I turnback."),
        QuizQ("The relative minor of C major is…", listOf("A minor","E minor","D minor","G minor"), 0, "Same signature, minor home."),
        QuizQ("V → I is which cadence?", listOf("Authentic","Plagal","Deceptive","Half"), 0, "The full-stop cadence."),
        QuizQ("IV → I is which cadence?", listOf("Plagal ('Amen')","Authentic","Half","Deceptive"), 0, "Churchy, gentle close."),
        QuizQ("The bass note of a 2nd-inversion triad is the…", listOf("5th","root","3rd","7th"), 0, "C/G — fifth in the bass."),
        QuizQ("The middle line of the ALTO clef is…", listOf("C4","A4","F3","G4"), 0, "The C-clef's center marks middle C."),
        QuizQ("Swung eighths are closest in feel to…", listOf("Triplets (2/3 + 1/3)","Even 16ths","Quintuplets","Dotted 16ths"), 0, "The pair fills a triplet: long–short."),
        QuizQ("6/8 time contains how many BEATS per bar?", listOf("2","6","3","4"), 0, "Two dotted-quarter beats — compound duple."),
        QuizQ("'Compound' meter means the beat divides into…", listOf("3","2","4","7"), 0, "Simple divides in 2; compound in 3."),
        QuizQ("Karsilama (Turkish belly-dance) is in…", listOf("9/8 (2+2+2+3)","4/4","7/8 (2+2+3)","6/8"), 0, "Aksak — 'limping' meters."),
        QuizQ("A dotted quarter equals how many eighths?", listOf("3","2","4","1.5"), 0, "Quarter = 2 eighths; the dot adds half again = 3.")
    )
}
