# Documentation Style Guide for Connector SDK Partners

Use this style guide to help you write effective documentation for Fivetran SDK connectors.

For technical language and formatting not specified here, follow the [Google developer documentation style guide](https://developers.google.com/style).

For general spelling and usage, follow [Merriam-Webster.com](https://www.merriam-webster.com/). For other style questions not covered here, follow AP Style.

## Voice

Fivetran advocates for simple, friendly technical communication that’s easily understood by international audiences. Adhere to the following best practices:

* Use plain language/plain English practices**.**
* Use simple international English.
* Use simple words.
* Prefer short sentences.
* Prefer the use of active voice.
* Use the present tense.
* Use U.S. date format (month/day/year).

**Talk to the users, not about them.**

**You**, not the user.

* RIGHT: You can select which tables to sync.
* WRONG: The user can select which tables to sync.

### What doesn’t belong in the documentation?

* Don’t use language that points to a specific point in time (e.g., now, new, yet, currently, in the future, soon, etc.) Refer to [Google’s guide to writing timeless documentation](https://developers.google.com/style/timeless-documentation).
* Don’t make vague promises about the future in the documentation. The documentation represents the present reality.
* Don’t add information or language you wouldn’t expect in technical documentation (e.g., persuasive language, marketing language, comparing with other products, etc.)
* Don’t put to-do notes about missing parts in the documentation. The documentation must be complete.
* Avoid plagiarism. Never copy-paste content from another source. Paraphrasing is OK, but even then, try to add more value to the content.

## Procedural docs (step-by-step docs)

Number the steps. Do not use indefinite or definite articles in step headings. For example, "Run `some_command` in your database cluster", "Create new user in Fivetran".

Write one action per step.

* RIGHT: 1\. Select **Foo** in the navbar. 2\. Click **OK**.
* WRONG: 1\. Select **Foo** in the navbar, then click **OK**.

When referring to a step, use the name of that step, *not* the number. For example, "the Finish Fivetran configuration" step (see [example](https://fivetran.com/docs/connectors/databases/planetscale/setup-guide#finishfivetranconfiguration)).

## Screenshots

* Use screenshots when a visual makes your point more clear.
* Don’t use screenshots for text-only elements. For example, if you write “Click OK,” you don’t need a screenshot of the OK button.
* Make a screenshot of the smallest possible area to show only the relevant information.
* Close any elements (modals, popups) that don't have the info you're trying to show.
* Minimize unnecessary empty space by resizing the browser window.
* Avoid both very wide and very long screenshots since they may not display well in the documentation.
* All screenshots in Fivetran documentation are .png images.

## Links/URLs

When linking to a documentation page (internal or external), use the page or section name in your anchor text to be more specific. That way, users still know what information to look for if the link breaks.

* RIGHT: Learn more in [Facebook's Budgets documentation](https://developers.facebook.com/docs/marketing-api/bidding/overview/budgets/).
* WRONG: Learn more in [Facebook's documentation](https://developers.facebook.com/docs/marketing-api/bidding/overview/budgets/).
* WRONG: [Click here](https://developers.facebook.com/docs/marketing-api/bidding/overview/budgets/) to learn more.

Use “see” or “learn more” to refer to links and cross-references.

* RIGHT: For more information about data storage platforms that we support, see [our Destinations documentation](https://fivetran.com/docs/destinations).

For further information and examples, see [Google’s Cross-References guide](https://developers.google.com/style/cross-references).

### Placeholder values for examples and screenshots

**Names:** Firstname Lastname

**User Icon:** Default empty headshot

**Passwords:** mypassword

## Placeholder variables in code blocks

Prefer descriptive names for what the variable does rather than examples or nonsense words.

Format placeholder values in italics. For italics to work in Fivetran docs, you must set the language to shell:

````
```shell
GRANT USAGE ON SCHEMA "some_schema" TO <em>username</em>;
```
````

## General language usage

Adhere to the following general language guidelines:

- Use American English spelling.
- Adhere to AP style unless specified otherwise.
- Use sentence case for titles and headings.
- Don’t use end punctuation in headers or subheaders.

### Titles and headers

Use title case for the page title and sentence case for all subsequent headers. In public-facing Fivetran docs, the title is H1, `#` in Markdown.

* Use H1 for main titles, H2 for sections, and H3 for subsections to make content easier to read and crawl.
* Headings must go in order (H1 → H2 → H3 → H4). Don’t skip levels.
* Use only one H1 per page.
* Create unique and descriptive titles for each page.
* Top-level headings (H2) should communicate what's most important to the article.
* Don't overuse headings. If you are introducing a heading for just a single paragraph of content, consider whether the heading is really necessary.
* Keep headings short. Be specific and put the most important idea first.
* Avoid following a heading with another heading with no content in between.

### Tense

Use present tense, not future tense. **Fivetran loads your tables**, *not* Fivetran will load your tables.

### Plagiarism

Never copy-paste content from another source. Paraphrasing is fine, but even then, try to add more value to the content.

### Table names vs. column names

* Write table names in **ALL\_CAPS**.
* Write column/ field names in all **lower\_case**.

### Code formatting

Use code style for code examples, keywords, values, table names, and field/column names. For example, the boolean keywords/values `true` and `false`, the string/value `“foo”`, the integer `1234`, the keyword `print`, the table name `ALL_CAPS`, and the column name `lower_case` should all be code-styled.

### Readable and scannable content

Use bullet points, short paragraphs, and concise sentences to make content easily digestible.

Use tables to make complex information more easily understood by presenting it in a clear structure. Make sure the purpose of the table is straightforward. Include a table title or brief introduction to set the tone if necessary.

### Bold

Use bold to indicate UI elements. For example: Click **Save & Test**.

### Emphasis

Use *italics* to emphasize a piece of information. Use italics sparingly. Never use all caps for emphasis.

### Notes

Use note style ( `>` in Markdown) for notes. Do not use the `{% info %}` note style. How you introduce your note depends on the urgency of its content:

* WARNING: The most urgent note type. Tells users how to avoid critical errors, like breaking their connector or overloading their destination. For example:

WARNING: MySQL databases can fail to perform basic queries for even medium volumes of data and is inappropriate for a data warehouse.

* IMPORTANT: The second-most urgent note type. Tells users how to set up and use Fivetran successfully. For example:

IMPORTANT: You must have TLS enabled on your database to connect to Fivetran directly.

* NOTE: The second-least urgent note type. Provides additional context or explanations about the document's content. For example:

NOTE: If you have a Standard or Enterprise account, you can also manage your Fivetran account and its connectors using our REST API.

* TIP: The least urgent note type. Contains your recommendations and tips; users don't need this information, but it's nice for them to know. For example:

TIP: Click **Show** to view your Secret Key.

### Data types

When listing data types, write their names in all caps, but *do not* put them in code style. For example, INTEGER, STRING, BINARY.

### Oxford comma

Use the [serial (Oxford) comma](https://www.grammarly.com/blog/what-is-the-oxford-comma-and-why-do-people-care-so-much-about-it/) in lists of three or more items. Serial commas make lists easier to read. They also prevent ambiguity. For example:

I love my parents, Jane and John.

Without the serial comma, you can interpret this sentence in two ways:

1. I love my parents, Jane, and John.
2. I love my parents, and my parents are Jane and John.

The serial comma, however, leaves no room for interpretation:

	I love my parents, Jane, and John.

### Percent (%)

Use the symbol (%) instead of spelling out percent.

## Ampersand (&)

Avoid using in-body text; use “and” instead. Also, avoid using headlines/headers and subheaders unless necessary for space or design.

### Courtesies (do not use\!)

When writing docs, instructions, and error messages, avoid *please*, *thank you*, and so on. Save your reader's time.

* **Correct**: Click the button to update your account settings.
* **Incorrect**: We request that you please click the button to update your account settings. Thank you\!

### Spell out an acronym in its first use

Don’t assume the reader knows an acronym. When first mentioning an acronym, write the full term with the acronym in parentheses.

### Numbers

Spell out numbers one through nine, and use figures for 10 and above.

Generally, use numerals with *million*, *billion*, *trillion*, etc.:

* *They built 1 billion new units*.

Not necessary with casual uses:

* *She could think of a billion reasons to decline the offer.*

When starting sentences, always spell out numbers:

* *Thirty-four days and nine hours later, he reconsidered.*

### Date/ time

Generally, spell out months. Use standard date notation format. For example, January 1, 2020, *not* January 1st, 2020\.

Use **a.m./p.m.** *not* **AM/PM**.

### Parentheses

Generally, use parentheses sparingly, as they pull the reader out of the flow of the sentence. Keep the information inside parentheses short, and consider em dashes or commas instead. If you’re trying to stuff a lot of information into parentheses, you should probably rewrite the sentence or break it in two.

**Correct:** They were frustrated with ineffective data movement methods (including DIY) as they expanded.

**Incorrect:** They were frustrated with ineffective data movement methods (including DIY and legacy platforms such as ABC and DEF that ate time and resources) as they expanded.

### Bullets

Use bullets when there is a list of three or more items. If the list contains one or two items, simply include it in a sentence. 

**Example**:

We have added two new tables, \`PRODUCT\_DIMENSION\_PERFORMANCE\_REPORT\` and \`PRODUCT\_DIMENSION\_IMPRESSION\_PERFORMANCE\_REPORT\`.

**not**

We have added two new tables:

* \`PRODUCT\_DIMENSION\_PERFORMANCE\_REPORT\`
* \`PRODUCT\_DIMENSION\_IMPRESSION\_PERFORMANCE\_REPORT\`

### Postscripts

Mark all relevant fields with an asterisk (`*`) to include a postscript. Add your postscript in note format and begin the note with an asterisk (`*NOTE:`).

While asterisks are a special character in Markdown, you do *not* need to escape an asterisk in note format. If you need to use two asterisks in the same sentence, put a space before the second one to avoid italics ([the automatic Markdown formatting](https://www.markdownguide.org/basic-syntax/#italic)). See [example of a postscript](https://fivetran.com/docs/destinations/clickhouse#typetransformationmapping).

## Release phases

We release new features, connectors, and destinations in phases to ensure we provide our users with the highest-quality experience. Below, we have outlined the expected user experience for each phase.

| PHASE               | DEFINITION                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Private Preview     | We release private preview versions of new features, connectors, or destinations to a small group of customers to verify functionality and address issues. Releases in the private preview phase are hidden from non-participating users. Private preview releases are likely to be missing some functionality, and known or unknown issues may surface. We cannot guarantee a quick resolution to these issues during the private preview phase. When a feature, connector, or destination is in Private Preview, its [Monthly Active Rows (MAR)](https://fivetran.com/docs/usage-based-pricing#monthlyactiverows) are free. |
| Beta                | We use beta releases to test functionality with a broader audience. Beta releases are available to any customer who wishes to use them. In beta, the functionality is complete, and the goal is to test more edge cases. We address issues as soon as possible according to our Support SLA.                                                                                                                                                                                                                                                                                                                                  |
| Generally Available | We release features or connectors as generally available once we have validated their quality and are sure we’ve identified all major technical issues in the previous two phases. If problems arise, we respond and address them as soon as possible according to our Support SLA.                                                                                                                                                                                                                                                                                                                                           |
| Sunset              | We sunset a feature, connector, or destination once we have identified a better solution that requires a breaking change. We notify users at least 90 days in advance of a sunset.                                                                                                                                                                                                                                                                                                                                                                                                                                            |

## Useful documentation tools

Convert Google Docs to Markdown. Use Google Docs built-in functionality to [export a Google Doc as Markdown](https://support.google.com/docs/answer/12014036#zippy=%2Cexport-a-google-doc-as-markdown) or [copy Google Docs content as Markdown](https://support.google.com/docs/answer/12014036#zippy=%2Ccopy-google-docs-content-as-markdown).  