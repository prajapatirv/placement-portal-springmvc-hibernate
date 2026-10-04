package com.ppsu.placement.demo;

import java.util.List;

/** One Claude artifact built for the session. Links are private to the owner until shared from the artifact's Share menu. */
record ArtifactLink(String group, String title, String purpose, String url) {

    static final String WALKTHROUGH = "Walkthrough (the three parts shown above)";
    static final String PRESENTATION = "Presentation and index";
    static final String GUIDES = "Guides and session material";

    /** Keep in step with docs/08-artifact-links.md. */
    static final List<ArtifactLink> ALL = List.of(
            new ArtifactLink(WALKTHROUGH, "Part 1: ORM with Hibernate, and where AI connects", "Mapping, Session, lifecycle, N+1, where AI plugs in", "https://claude.ai/artifact/TA4zJFNPtaRhSUwnzdDv27"),
            new ArtifactLink(WALKTHROUGH, "Part 2: Spring MVC and the Placement app", "DispatcherServlet, beans, forms, the five phases", "https://claude.ai/artifact/Gh32D4cuCxvw9DJoRdbuFU"),
            new ArtifactLink(WALKTHROUGH, "Part 3: Gen AI, from basics to the whole stack", "AI ladder, grounding, tool loop, AI Desk, whole stack", "https://claude.ai/artifact/MCtoGsEd4FTa4FWSN7Uhha"),
            new ArtifactLink(PRESENTATION, "Placement Portal Deck", "16-slide animated presentation", "https://claude.ai/artifact/Y5g3nyUVdpZpHpmgRhJ2Ph"),
            new ArtifactLink(PRESENTATION, "Placement Portal: Artifact Index", "Document listing all artifacts", "https://claude.ai/code/artifact/a27f9918-2a9b-4c6b-afd2-26f620d037dd"),
            new ArtifactLink(GUIDES, "Complete Feature Guide", "Every feature, module by module", "https://claude.ai/artifact/Q2kzcVsq5i6vrBSq4XDjYG"),
            new ArtifactLink(GUIDES, "Code Walkthrough and Session Guide", "Package-by-package code tour", "https://claude.ai/artifact/ApDtpyjCBmwSuvaXJETCZp"),
            new ArtifactLink(GUIDES, "Industry Expert Session: Advance Java + Gen AI", "Session plan and PPT prep guide", "https://claude.ai/artifact/DMLu2LR5MvgU4pv31ALm36"),
            new ArtifactLink(GUIDES, "PPSU Session 1: PPT Slide Preparation", "Slide content, Part 1 and Part 2", "https://claude.ai/artifact/9p1a2Ajd6Jd4LkzJrmiS2U"),
            new ArtifactLink(GUIDES, "PPSU Session 2: Spring MVC + Hibernate + Supabase Demo Guide", "Live demo steps", "https://claude.ai/artifact/8LGqbJcqMTxR8UbjkqUtS9"),
            new ArtifactLink(GUIDES, "PPSU Session 3: Gen AI Module on the saaviragroup.in Dashboard", "Gen AI module walkthrough", "https://claude.ai/artifact/QUdbK6GuDk5uQodUUDDyQR"),
            new ArtifactLink(GUIDES, "PPSU Session 4: Speaker Scripts and Fillers", "Speaker scripts, Part 1 and Part 2", "https://claude.ai/artifact/KJzN4inTMSR3zvuZ6i81N2"),
            new ArtifactLink(GUIDES, "Gen AI Pipelines", "Gen AI pipeline explainer", "https://claude.ai/artifact/TUx3iXai1ceXzyt6g41LRt"),
            new ArtifactLink(GUIDES, "AI Desk & Q&A: Slide Prep", "Slide prep for AI Desk and Q&A", "https://claude.ai/artifact/Q2oFex9mF7cqH57whTft4Z"),
            new ArtifactLink(GUIDES, "AI Desk Module: Change Document & Impact Assessment", "Change record and impact", "https://claude.ai/artifact/3cDea6Z58VgsHnbKsF1R8j"));
}
