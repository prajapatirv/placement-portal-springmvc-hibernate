# PPSU Session 4 (v3): Part 2 Speaker Script, Gen AI made simple

Written for fifth-semester students. The deck has 30 slides plus 2 hidden backups. Every line below is also in the speaker-notes pane of the PPTX, so the slides and this script cannot drift apart.

**What changed from the earlier Part 2 script:** the AI Desk and placement-assistant demo is gone, along with JSON, tools, MCP, security, testing and cost slides, and the "same email, two prompts" slide. In their place: plain-language slides on AI, LLM, RAG, AI agents and agentic AI (following the LLM vs RAG vs Agent vs Agentic AI comparison from the Zapier presentation and your AI-term images), a hands-on lab where students build a mini RAG by hand with any chatbot, and short games that keep the room awake.

## 1. How to use this script

Speak it, do not read it. Read it three times, then talk from the slide. Keep every sentence short; use the example given for each slide. The running pictures are the same as the morning: the **canteen** and the **new intern** (brilliant, fast, needs clear instructions and checking).

**Cue marks**

| Mark | Meaning |
| --- | --- |
| [CLICK] | Advance the animation. Each click reveals one idea (fade). |
| [DEMO] | Switch to a chatbot in the browser (one demo only) |
| [LAB] | Students work on their own phone or laptop |
| [POLL] | Ask the room and count hands |
| [PAIR] | Thirty seconds with the person next to you |
| [DISCUSS] | Two minutes of open discussion |
| [FILLER: name] | A joke or analogy from the bank in section 5 |
| [PAUSE] | Two seconds of silence |

**Slide behaviour:** each block has its own colour (teal basics, orange talking to AI, coral when AI is wrong, violet smarter AI, sky industry, gold product and close). Game slides use a split transition, the demo zooms in, block openers push in from the right, the break dissolves. Answers on game slides stay hidden until you click, so the room has to guess first.

**Voice rules:** one face per sentence; light Hinglish only where natural; ask a question at least every five minutes and wait three full seconds; say "I got this wrong once" twice; never apologise for a failed demo, use the lines in section 7.

## 2. Timing map (starts 13:20, times are approximate)

| Block | Starts | Minutes | Slides |
| --- | --- | --- | --- |
| Opening | 13:20 | 5 | 1 to 3 |
| The basics | 13:25 | 15 | 4 to 8 |
| Talking to AI | 13:40 | 11 | 9 to 12 |
| When AI is wrong | 13:51 | 8 | 13 to 15 |
| Smarter AI | 13:59 | 28 | 16 to 21 |
| Break | 14:27 | 10 | 22 to 22 |
| AI in real life | 14:37 | 6 | 23 to 24 |
| Close | 14:43 | 9 | 25 to 30 |

Q&A starts around **14:52** (slide 29). The plan holds 45 minutes of Q&A, which leaves about **53 spare minutes** before 16:30. Section 8 says how to use them. The afternoon is deliberately shorter and simpler; do not rush to fill it.

## 3. Interaction timeline

| Clock | Slide | Type | What | Time |
| --- | --- | --- | --- | --- |
| 13:21 | 2 | STAND + HANDS | Quick show of hands | 3 min |
| 13:37 | 8 | MYTH or FACT, hands | Myth or fact? | 3 min |
| 13:43 | 10 | PAIR, 30 seconds | Prompt makeover | 3 min |
| 13:48 | 12 | DISCUSS 2 min | Use AI responsibly | 3 min |
| 13:53 | 14 | DEMO | Ask about something from today | 4 min |
| 14:03 | 17 | LAB, 10 minutes | Lab: build a mini RAG by hand | 12 min |
| 14:24 | 21 | GAME: shout the answer | LLM, RAG, agent or agentic AI? | 3 min |
| 14:27 | 22 | PAIR teach-back | Stretch break | 10 min |
| 14:45 | 26 | QUIZ: hands up A or B | Rapid-fire quiz | 3 min |
| NaN:NaN | 29 | Q&A, raise your hand | qa | 0 min |

## 4. Part 2 script, slide by slide

### Opening (from 13:20, slides 1 to 3)

**Slide 1: Title** (about 13:20, 1 min)

*Purpose:* Start standing, warm and short. Connect to the morning.  
*Remember:* Today we add the third layer: intelligence.

"Welcome back. [PAUSE] This morning we looked at two layers: data with Hibernate and web with Spring MVC. This afternoon is the third layer, intelligence. I will not make this complicated. By the end you will be able to explain what an LLM, RAG and an AI agent are, why AI sometimes gets things wrong, and how to use it well."

*Example to give:* Point at the three-layer picture: the same one as the morning.  
*If it fails:* None.

**Slide 2: Quick show of hands** [CLICK x1] (about 13:21, 3 min)

*Purpose:* Wake the room after lunch and find out who already uses AI.  
*Remember:* You already use AI. Today you learn how it works.

"Quick test of the post-lunch brain. Stand up if you had more than two rotis. [PAUSE] Sit down if you are thinking about a nap. [laughter] Good, we are all human. Now hands only. [POLL] Who used an AI chatbot today? This week? Never? Who has called an AI from code? [count each] [CLICK] So you already use AI. Today you learn how it works, where it fails, and how to use it like an engineer."

*Example to give:* Ask one student: "What did you ask it?" Use their answer as an example later.  
*Filler:* the post-lunch nap negotiation  
*If it fails:* If nobody responds, say "I will go first" and raise your own hand.

**Slide 3: Five steps, simple to smarter** [CLICK x5] (about 13:24, 1 min)

*Purpose:* Give the afternoon a simple map.  
*Remember:* Five steps, from basic to smarter.

"Five steps, like stairs in the college library. [CLICK] One: the basics, what AI and an LLM are. [CLICK] Two: talking to AI, how to ask well. [CLICK] Three: when AI is wrong, called hallucination. [CLICK] Four: smarter AI, RAG, agents and agentic AI. [CLICK] Five: how real companies use it, and a real product I built."

*Example to give:* Library stairs: you do not skip floors.  
*If it fails:* None.

### The basics (from 13:25, slides 4 to 8)

**Slide 4: One of these three is wrong** [CLICK x1] (about 13:25, 4 min)

*Purpose:* Game that shows AI can sound sure and still be wrong.  
*Remember:* Confidence is not correctness.

"Three answers from an AI, all in the same confident voice. One is wrong. Vote with your hands: one, two or three. [POLL] [PAUSE for votes] [CLICK] Number two is wrong. In Java, String is a class, not a primitive type. Look at what happened: the wrong answer sounded just as sure as the true ones. That is called hallucination: confident and wrong. Remember this feeling; we come back to it."

*Example to give:* Many freshers also confuse String with a primitive type, so this is a believable mistake.  
*Filler:* the confident canteen friend  
*If it fails:* If votes split evenly, say "Perfect, that is exactly why it is dangerous".

**Slide 5: The AI family: circles inside circles** [CLICK x4] (about 13:29, 3 min)

*Purpose:* Explain AI, ML, deep learning and generative AI as circles.  
*Remember:* Gen AI is the part of AI that creates new things.

"Four words you hear everywhere, as circles inside circles. [CLICK] AI is the big circle: machines doing things that need human intelligence. Example: a chess program. [CLICK] Machine learning: the machine learns from data instead of fixed rules. Example: your email spam filter learned from emails people marked as spam. [CLICK] Deep learning: a way of learning with many layers, like a brain with many steps. Example: voice assistants and face unlock. [CLICK] Generative AI: it creates new text, images and code. ChatGPT, Gemini and Claude are examples. The part that works with text is called an LLM, and that is next."

*Example to give:* Spam filter for machine learning; face unlock for deep learning; ChatGPT for generative AI.  
*If it fails:* None.

**Slide 6: LLM: a very smart autocomplete** [CLICK x4] (about 13:32, 3 min)

*Purpose:* Explain what an LLM is in the simplest way.  
*Remember:* An LLM is a very smart autocomplete. It forgets when the chat ends.

"LLM means Large Language Model. [CLICK] Here is the whole picture: you write a prompt, the LLM reads it, and writes an answer. [CLICK] It has read a huge amount of text. [CLICK] Its job is to guess the next word, again and again, like the suggestions above your phone keyboard, but much smarter. [CLICK] And when the chat ends, it forgets. Tell it your name in one chat, open a new chat, and it does not know you."

*Example to give:* Phone keyboard suggestions, but trained on a giant library.  
*Filler:* keyboard suggestions  
*If it fails:* None.

**Slide 7: AI reads in small pieces called tokens** [CLICK x2] (about 13:35, 2 min)

*Purpose:* Introduce tokens and the context window with one picture each.  
*Remember:* AI reads in small pieces called tokens, and can only keep so much in view.

"AI does not read whole words. It cuts them into small pieces. Look: a word like 'programming' can be cut into 'pro', 'gram', 'ming'. The exact split depends on the AI, but the idea is the same. These pieces are called tokens. [CLICK] Why do we care? AI services count tokens, like SMS packs: you pay for what you send and what you get back. [CLICK] The context window is the size of your study desk. Only so many books fit open at once; older pages fall off the desk. That is why very long chats sometimes forget the beginning."

*Example to give:* SMS packs for tokens; study desk for context window.  
*Filler:* SMS packs, study desk  
*If it fails:* If asked about temperature, say: "It is how creative the answer is; low for exact work, high for poems".

**Slide 8: Myth or fact?** [CLICK x4] (about 13:37, 3 min)

*Purpose:* Quick myth-or-fact round.  
*Remember:* AI can be wrong, and it does not know today's news by itself.

"Myth or fact? Right hand up for fact, left hand up for myth. One: 'AI always tells the truth.' [PAUSE for hands] [CLICK] Myth. Two: 'AI can sound confident and still be wrong.' [PAUSE] [CLICK] Fact. Three: 'A better question gives a better answer.' [PAUSE] [CLICK] Fact, and that is our next topic. Four: 'AI knows what happened this morning by itself.' [PAUSE] [CLICK] Myth. It knows what it was trained on, unless it is connected to the internet."

*Example to give:* Ask it who won yesterday's match without search: it may guess.  
*Interaction:* MYTH or FACT, hands  
*If it fails:* If the room is tired, do it as a shout instead of hands.

### Talking to AI (from 13:40, slides 9 to 12)

**Slide 9: A good prompt has four parts** [CLICK x4] (about 13:40, 3 min)

*Purpose:* Teach a good prompt in four parts.  
*Remember:* Role, context, task, format: a clear question gets a clear answer.

"A prompt is the instruction you give AI. Treat it like briefing a new intern. [CLICK] Role: who should it be? 'You are a friendly Java tutor.' [CLICK] Context: what should it know about you? 'I am a fifth semester student; I know OOP basics.' [CLICK] Task: what should it do? 'Explain inheritance with one example.' [CLICK] Format: how should it answer? 'In five lines, simple words.' Give an intern a sticky note saying 'handle this' and you get chaos. Give a clear briefing and you get good work."

*Example to give:* Java tutor prompt for inheritance.  
*Filler:* briefing the new intern  
*If it fails:* None.

**Slide 10: Prompt makeover** [CLICK x2] (about 13:43, 3 min)

*Purpose:* Pair activity: improve a weak prompt.  
*Remember:* Add role, context, task and format and the answer improves.

"Your turn. The prompt is: 'Explain Java.' [PAUSE] With your neighbour, 30 seconds: make it better using role, context, task, format. [PAIR] [take two suggestions] [CLICK] Here is one version: 'Act as a Java interviewer for a fresher. I know OOP and collections but not Spring. Ask one question at a time. Give feedback in two lines.' [CLICK] See the four parts? Same AI, a very different experience. Try this tonight for interview practice."

*Example to give:* Interview practice prompt.  
*Interaction:* PAIR, 30 seconds  
*If it fails:* If the room is quiet, offer the first suggestion yourself.

**Slide 11: From Java, AI is just a web service call** [CLICK x3] (about 13:46, 2 min)

*Purpose:* Show that using AI from Java is like calling any web service.  
*Remember:* AI from Java is an API call. Keep the key secret.

"How does a Java developer use AI? The same way you call any web service. [CLICK] Your app sends the question to the AI service and gets the answer text back. [CLICK] You have used web services before; this is the same idea. [CLICK] One safety rule: the API key is like your ATM PIN. It stays on the server, never in the browser, never in GitHub. If you push a key to GitHub, bots find it within minutes."

*Example to give:* A Spring app that sends a question and shows the answer on a page.  
*Filler:* ATM PIN  
*If it fails:* None.

**Slide 12: Use AI responsibly** [CLICK x4] (about 13:48, 3 min)

*Purpose:* Responsible use of AI in four simple points.  
*Remember:* Privacy, fairness, copyright, honesty.

"Four points about using AI responsibly. [CLICK] Privacy: would you paste a friend's phone number or your Aadhaar into a public chatbot? [DISCUSS 2 min, take answers] [CLICK] Fairness: AI learned from people, so it can copy human bias. [CLICK] Copyright: not everything it writes is free to copy. [CLICK] Honesty: say when something is AI-assisted. This is what makes you a good engineer, not only a fast one."

*Example to give:* Pasting private data into a public chatbot.  
*Interaction:* DISCUSS 2 min  
*If it fails:* None.

### When AI is wrong (from 13:51, slides 13 to 15)

**Slide 13: Hallucination: confident and wrong** [CLICK x3] (about 13:51, 2 min)

*Purpose:* Define hallucination and why it happens.  
*Remember:* A confident answer that is not true.

"Hallucination: a confident answer that is not true. Examples: a Java method that does not exist, like 'list.sortDescending()'; a book nobody wrote; a rule your college never made. Why does it happen? [CLICK] It guesses what sounds right; it does not check facts. [CLICK] It has no source of truth unless you give it one. [CLICK] Its knowledge stops at a date, so yesterday's news is invisible. It is not lying. It is a very confident autocomplete."

*Example to give:* An invented method that only exists in the AI's imagination.  
*Filler:* the confident canteen friend  
*If it fails:* None.

**Slide 14: Ask about something from today** [CLICK x1] (about 13:53, 4 min)

*Purpose:* Live proof that AI answers confidently about things it cannot know.  
*Remember:* Fluent is not the same as correct.

"Let me show you. [DEMO: open a chatbot with no web search, ask about something that happened today, for example a headline or a match score] Read the answer aloud. It sounds sure. No source, no date. Now ask: 'What is your source?' [wait] [CLICK] Fluent is not the same as correct. Keep that sentence in mind whenever you use AI for an assignment."

*Example to give:* Ask for today's match score or a news headline.  
*If it fails:* Use the backup slide with a saved screenshot. Say: "Same question, recorded this morning."

**Slide 15: Three habits to stay safe** [CLICK x3] (about 13:57, 2 min)

*Purpose:* Three simple habits to avoid being fooled.  
*Remember:* Ask for the source, give it the document, check important facts yourself.

"Three habits. [CLICK] One: ask for the source, then open the source. If it cannot give one, be careful. [CLICK] Two: give it your own document and tell it to answer only from that. That idea has a name, and it is the next slide. [CLICK] Three: check important facts yourself, especially marks, money, medicine and law. AI is a great first draft and a poor final judge."

*Example to give:* Before you submit an assignment, verify every fact it gave you.  
*If it fails:* None.

### Smarter AI (from 13:59, slides 16 to 21)

**Slide 16: RAG: AI with an open book** [CLICK x5] (about 13:59, 4 min)

*Purpose:* Explain RAG with the open-book exam picture.  
*Remember:* RAG = AI that looks in your documents before it answers.

"RAG stands for Retrieval-Augmented Generation. Big name, simple idea: an open-book exam. [CLICK] Step one: you ask a question. [CLICK] Step two: the system searches your files. [CLICK] Step three: it reads the relevant parts. [CLICK] Step four: the AI writes the answer using what it found. [CLICK] Without RAG it is a closed-book exam: the AI answers from memory and may guess. With RAG it opens your approved pages first. Example: a college chatbot that answers 'What is the minimum CGPA for placement?' by reading the placement rules PDF."

*Example to give:* College chatbot reading the placement rules PDF.  
*Filler:* the open-book exam  
*If it fails:* None.

**Slide 17: Lab: build a mini RAG by hand** [CLICK x4] (about 14:03, 12 min)

*Purpose:* Hands-on lab: students build a tiny RAG by hand with any chatbot.  
*Remember:* Give it a document and tell it to answer only from the document.

"Lab time, 10 minutes. Open any AI chatbot on your phone or laptop. [LAB] [CLICK] Step one: paste a short paragraph. Use your college rules, your timetable, or any notes. Do not paste private data. [CLICK] Step two: type: 'Answer ONLY from the text above. If the answer is not there, say not in the document.' [CLICK] Step three: ask two questions: one that IS in the text, and one that is NOT. [walk around the room, ask three students what happened] [CLICK] You just built RAG by hand. The document was the open book, and the second question showed you 'I do not know' working properly. One difference: in a real system, the computer searches many documents and picks the right pages for you. You did that step by hand. That is how companies make AI safer."

*Example to give:* Paste the rules of a college event, then ask about a rule that does not exist.  
*Interaction:* LAB, 10 minutes  
*If it fails:* If a phone has no internet, pair students so one person with data can share. If nobody can connect, run it on your own laptop and let students dictate questions.

**Slide 18: AI agent: AI that does the task** [CLICK x7] (about 14:15, 4 min)

*Purpose:* Explain an AI agent as AI that does a task, not just answers.  
*Remember:* An agent makes a plan, uses tools, checks, and repeats.

"A chatbot answers. An agent does the task. [CLICK] You give it a goal. [CLICK] It makes a plan. [CLICK] It uses tools. [CLICK] It checks the result. [CLICK] If it is not right, it adjusts and tries again, then it is done. [CLICK] Tools can be web search, a calculator, email and files. [CLICK] Example: 'Find three Java internships, compare them and write me a short summary.' The agent searches, reads, compares and writes. That is a lot of power, so we keep a human watching the important steps."

*Example to give:* Find three Java internships, compare them, write a summary.  
*Filler:* the intern with a to-do list  
*If it fails:* None.

**Slide 19: Agentic AI: a team of AI agents** [CLICK x5] (about 14:19, 3 min)

*Purpose:* Explain agentic AI as a team of AI agents.  
*Remember:* Agentic AI is a team of AI workers, each with one job.

"Agentic AI is a team of agents, each with one job, working together. [CLICK] A manager AI splits the work. [CLICK] A researcher AI finds information and a writer AI writes. [CLICK] A reviewer AI checks. [CLICK] The team delivers the final result. [CLICK] Example: planning your college fest campaign. One AI researches what students like, one writes posts, one checks the tone, and a person approves. More power, but also more things that can go wrong, so more supervision."

*Example to give:* College fest campaign with a research AI, a writing AI and a review AI.  
*Filler:* the project group  
*If it fails:* None.

**Slide 20: LLM, RAG, agent, agentic AI** [CLICK x1] (about 14:22, 2 min)

*Purpose:* One table that fixes the four terms.  
*Remember:* Start simple. Use the smallest one that solves the problem.

"One table to remember the four. LLM: smart text generator. RAG: answers from your documents. Agent: plans and uses tools to do a task. Agentic AI: a team of agents. Look at the bottom row for real examples. [CLICK] Rule: start simple. You do not hire a team to carry one bag."

*Example to give:* Do not hire a team to carry one bag.  
*If it fails:* None.

**Slide 21: LLM, RAG, agent or agentic AI?** [CLICK x4] (about 14:24, 3 min)

*Purpose:* Game: match each situation to LLM, RAG, agent or agentic AI.  
*Remember:* Choose the smallest tool that does the job.

"Game: LLM, RAG, agent or agentic AI? I read a situation, you shout the answer. One: 'Write a poem about the monsoon.' [PAUSE] [CLICK] LLM, plain text is enough. Two: 'Answer questions from our college rulebook PDF.' [PAUSE] [CLICK] RAG, because it needs documents. Three: 'Search the web, compare three laptops and email me a summary.' [PAUSE] [CLICK] An agent, because it uses tools and does steps. Four: 'One AI researches, one writes and one reviews a full marketing plan.' [PAUSE] [CLICK] Agentic AI, a team."

*Example to give:* Poem, rulebook PDF, laptop comparison, marketing plan.  
*Interaction:* GAME: shout the answer  
*If it fails:* None.

### Break (from 14:27, slides 22 to 22)

**Slide 22: Stretch break** [CLICK x2] (about 14:27, 10 min)

*Purpose:* Break plus an energiser so the afternoon does not sag.  
*Remember:* Stand up. Teach a neighbour. Come back sharp.

"Ten minutes. Stand up and roll your shoulders. [CLICK] While you stand: turn to the person next to you and explain 'LLM' in one sentence. [CLICK] Now explain RAG as an open-book exam. If you cannot, ask me when we are back."

*Example to give:* Neighbour teach-back: LLM and RAG.  
*Interaction:* PAIR teach-back  
*If it fails:* If the university has not confirmed a break, skip this slide and do only the stand-up.

### AI in real life (from 14:37, slides 23 to 24)

**Slide 23: Where companies use AI** [CLICK x6] (about 14:37, 3 min)

*Purpose:* Show where companies use AI today.  
*Remember:* Where the stakes are high, a human stays in charge.

"Where is AI used for real? [CLICK] Banks: spotting fraud, reading documents, support chat. [CLICK] Hospitals: summarising notes; the doctor still decides. [CLICK] Online shopping: search, recommendations, review summaries. [CLICK] Customer support: chatbots that answer from company documents, that is RAG. [CLICK] Software teams: code help, writing tests, code review. [CLICK] Hiring: sorting resumes, with a human making the final call. Notice the pattern: the more serious the decision, the more a human stays in the loop. You will meet these tools in your first job."

*Example to give:* Doctor decides; recruiter decides.  
*If it fails:* These are general examples; if asked about a specific company, say "I will check and share".

**Slide 24: A real product: Jyotish AI by Saavira** [CLICK x5] (about 14:40, 3 min)

*Purpose:* Show one real product as a case study, in simple terms.  
*Remember:* Calculated first, explained after.

"This is a product I built: Jyotish AI by Saavira, at saaviragroup.in. It does Vedic astrology charts and numerology. The rule on the home page: the charts and numbers are calculated by code, and AI only explains what the maths has already verified. [CLICK] You enter your birth details. [CLICK] Code calculates, using fixed formulas. [CLICK] AI explains in simple words. [CLICK] You read a report you can trust. [CLICK] Why this order? Because AI is great at explaining but not at calculating. It cannot invent the numbers if it never calculates them. Same rule you can use in your own projects."

*Example to give:* Code gives the numbers; AI writes the explanation.  
*If it fails:* If the site is slow, do not open it; the slide is enough.

### Close (from 14:43, slides 25 to 30)

**Slide 25: Where to explore next** [CLICK x2] (about 14:43, 2 min)

*Purpose:* Tell students where to explore next. No QR codes.  
*Remember:* Build small and finish it.

"Where to go next. [CLICK] Learn the basics from the official guides and free short courses of the big AI companies. Build small: call an AI API from Java, try Spring AI or LangChain4j. Explore open models on Hugging Face. [CLICK] Practise on Kaggle, read real code, like this morning's Hibernate and Spring MVC repo, and follow release notes and one good newsletter. The links are in docs/09-resources.md in the repo; I will not put QR codes on the slides."

*Example to give:* Rebuild the morning's repo and add one small AI call.  
*If it fails:* None.

**Slide 26: Rapid-fire quiz** [CLICK x5] (about 14:45, 3 min)

*Purpose:* Rapid-fire quiz to lock in the five main ideas.  
*Remember:* Five questions, five takeaways.

"Rapid-fire. Hands up for A or B. One: LLM stands for A, Large Language Model, or B, Little Learning Machine. [CLICK] A. Two: RAG helps the AI by A, giving it your documents, or B, making the computer faster. [CLICK] A. Three: an AI agent can A, only chat, or B, plan and use tools. [CLICK] B. Four: when AI gives a confident wrong answer, it is called A, hallucination, or B, compilation. [CLICK] A. Five: who should check important AI answers, A, nobody, or B, you? [CLICK] B. Five out of five? You can now explain Gen AI to your family."

*Example to give:* Five quick takeaways.  
*Interaction:* QUIZ: hands up A or B  
*If it fails:* None.

**Slide 27: Gen AI in daily life: try it tonight** [CLICK x6] (about 14:48, 2 min)

*Purpose:* Give practical, safe things to try tonight.  
*Remember:* Use AI for the first draft and as a practice partner. You still sign the work.

"Six things you can try tonight. [CLICK] Understand a hard topic: 'explain transactions with a UPI example', then check your notes. [CLICK] Make revision cards from your own notes, without private data. [CLICK] Debug: paste the error and ten lines, and read the fix before you run it. [CLICK] Interview practice with the prompt we just built. [CLICK] Improve one resume line, keep your voice and keep facts true. [CLICK] Plan your study time, then actually follow it. Use AI for the first draft and as a practice partner. You are still the engineer who signs the work."

*Example to give:* UPI example for transactions.  
*If it fails:* None.

**Slide 28: A six-month roadmap** [CLICK x4] (about 14:50, 2 min)

*Purpose:* Give a simple six-month plan.  
*Remember:* One finished project beats ten half-finished tutorials.

"A six-month plan. [CLICK] Months one and two: core Java, SQL and Git, and rebuild this morning's repo yourself. [CLICK] Months three and four: REST APIs, security, Docker. [CLICK] Months five and six: one real project with a small AI feature, on GitHub, with a README. [CLICK] One finished project beats ten half-finished tutorials. Interviewers read GitHub."

*Example to give:* Placement portal plus one small AI feature, on GitHub.  
*If it fails:* None.

**Slide 29: Q&A** (about 14:52)

*Purpose:* Open the floor.  
*Remember:* No question is too basic.

"Your questions. Three areas: Java and Hibernate, Gen AI, and careers. No question is too basic; the basic ones help the most people. [PAUSE 3 seconds] If the room is quiet: 'Let me ask you a question, then.' and use one from the Q&A bank."

*Example to give:* Use the tough-question phrases in section 9.  
*Interaction:* Q&A, raise your hand  
*If it fails:* If it goes quiet, use five questions from the master plan Q&A bank.

**Slide 30: Closing** [CLICK x4] ()

*Purpose:* End on four simple lines students can remember.  
*Remember:* Learn the basics. Ask it clearly. Check its answers. Build something small.

"Four lines to take home. [CLICK] Learn the basics. [CLICK] Ask it clearly. [CLICK] Check its answers. [CLICK] Build something small. [PAUSE, smile for three seconds] Thank you, PPSU."

*Example to give:* Say it slowly, then stop talking.  
*If it fails:* None.

## 5. Part 2 filler bank: daily-life pictures for every idea

| Idea | Daily-life picture | Say it like this | Slides |
| --- | --- | --- | --- |
| LLM | Phone keyboard suggestions | "It guesses the next word, like your keyboard, but trained on a huge library." | 6 |
| Token | SMS pack | "You pay for what you send and what comes back." | 7 |
| Context window | The size of your study desk | "Only so many books fit open at once." | 7 |
| Hallucination | The confident friend in the canteen | "He explains everything with total confidence, including what he has never read." | 4, 13 |
| Prompt | Briefing a new intern | "'Handle this' gets chaos. Role, context, task, format gets good work." | 9, 10 |
| API key | Your ATM PIN | "Keep it on the server. Never in GitHub." | 11 |
| RAG | The open-book exam | "The AI is a bright student who has not read our syllabus. Give it the book." | 16, 17 |
| "I do not know" | The honest classmate | "The best answer is sometimes: it is not in the document." | 17 |
| AI agent | An intern with a to-do list | "A chatbot answers. An agent does the task." | 18 |
| Agentic AI | The college project group | "Each member has one job; together they finish the project." | 19 |
| Start simple | You do not hire a team to carry one bag | "Use the smallest tool that solves the problem." | 20, 21 |

**Light one-liners for tired moments (use two or three, not all)**

- "AI is like a very enthusiastic intern: fast, tireless, occasionally wrong with great confidence. Supervise accordingly."
- "If your demo works on the first try, check whether you are in the right room."
- "Our bug was not in the code. It was in my assumption. Those are the expensive ones."

**Post-lunch rescue if the room is flat before slide 4:** swap front and back rows for the next block, or ask the left half to explain 'hallucination' to the right half in one sentence.

## 6. Demo and lab run sheet

| Item | Slide | Steps | Backup |
| --- | --- | --- | --- |
| DEMO: AI answers about today's news | 14 | Open a chatbot without web search. Ask about something that happened today. Read the answer aloud. Ask "what is your source?". Click the banner. | Backup slide B1 (saved screenshot) |
| LAB: mini RAG by hand | 17 | Students open any chatbot. Paste a paragraph. Add the instruction "Answer ONLY from the text above...". Ask one question inside the text and one outside it. Walk around and ask three students what happened. | Do it on your laptop and take dictated questions |

**Before the session:** save a screenshot of the DEMO answer in the morning (slide B1). Keep a short sample paragraph ready to paste in the LAB (for example, the rules of a college event) and write two questions: one answered by it, one not.

## 7. Recovery lines

| Failure | Say | Then do |
| --- | --- | --- |
| Chatbot slow or down | "The AI is on a tea break too. Same question, recorded this morning." | Backup slide B1 |
| Wi-Fi unreachable | "The venue network has opinions. Here is the saved version." | Backup slide B1; for the LAB, students use mobile data |
| Students cannot connect in the LAB | "Pair up: one phone per two people." | Or run it on your laptop with dictated questions |
| Projector or adapter fails | "No slides, no problem, you get the whiteboard version." | Backup slide B2: draw LLM, RAG, Agent, Agentic AI |
| You blank on a line | "Let me say that in a different way." | Read the slide title and restate it in one sentence |
| Room is silent on a poll | "I will go first." | Raise your own hand, wait three seconds, give half a hint |

## 8. Timing recovery and spare-time plan

**If you are late (cut in this order):**

| Late by | Cut or shorten | Saves |
| --- | --- | --- |
| 5 minutes | Slide 7 (tokens) and slide 11 (Java API): one sentence each | 3 |
| 10 minutes | Shorten the LAB to 6 minutes; skip slide 20 (table), use the game on slide 21 instead | 8 |
| 15 minutes | Shorten the break to 5 minutes | 5 |
| 20 minutes or more | Skip slide 27 and say it in one sentence | 2 |

**If you have the spare 53 minutes** (Part 2 ends early by design), choose from these, in this order:

1. Give Q&A more room: open with "ask me anything about careers, AI or Java".
2. A second LAB round: students improve their prompt from slide 10 and compare results with a neighbour.
3. A career conversation: how to start, what to build, how to read job descriptions.
4. Walk through one more example of RAG or an agent that the students suggest.

Check with the course co-ordinator whether the session may end early. If so, stop after the closing slide.

## 9. Tough questions that come up in Part 2

| Question | Say |
| --- | --- |
| "Will AI take my job?" | "Gen AI is the first tool that understands a request in normal language. It will not replace developers who can design, check and secure systems; it will replace tasks. The person who knows how to use AI well is more valuable, not less. Calculators did not remove mathematicians; they removed long division." |
| "Which AI tool is best?" | "It changes every few months. Try two or three on the same question and keep the one that works for you." |
| "What is MCP?" | "A standard plug that lets AI apps connect to tools and files, like USB-C for AI. You do not need it to start." |
| "What is a vector database or embedding?" | "A way to search by meaning instead of exact words. It is how RAG finds the right page. Search for 'cheap laptop' and it finds 'budget notebook'." |
| "How do I build RAG in Java?" | "Start with the lab: a document plus a clear instruction. When you want code, look at Spring AI or LangChain4j." |
| "Why not Python for AI?" | "Python is excellent for training and experiments. Java is excellent for the business application that calls the AI safely at scale. Most real products use both." |
| "Is it safe to put my data in a chatbot?" | "Treat it like a postcard: do not write anything on it that you would not want read. Check the provider's data policy." |
| You do not know | "I do not know. Here is how I would find out." Then name the docs or the experiment. Never guess about security or legal matters. |
| Off-topic or too big | "Great question; it deserves ten minutes. Put it in the parking lot and I will answer it at the end." |

## 10. The last minute

End on the four lines, then stop talking and smile for three seconds before you thank them:

> **Learn the basics. Ask it clearly. Check its answers. Build something small.**

After the session: share the repo link, publish docs/09-resources.md, and answer the first five questions within a week.
