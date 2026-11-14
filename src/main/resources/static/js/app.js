const CATS = [
  {key:'channel', label:'Channel', color:'#db2777'},
  {key:'topic', label:'Topic', color:'#0891b2'},
  {key:'core', label:'Core', color:'#d97706'},
  {key:'database', label:'Database', color:'#7c3aed'},
  {key:'devops', label:'DevOps', color:'#dc2626'},
  {key:'monitoring', label:'Monitoring', color:'#059669'},
  {key:'programming', label:'Programming', color:'#2563eb'},
  {key:'shorts', label:'Shorts', color:'#c026d3'},
  {key:'lowcomp', label:'Low-comp', color:'#a16207'},
  {key:'hidden', label:'Hidden', color:'#4f46e5'},
  {key:'seo', label:'SEO', color:'#0d9488'},
];

let activeCats = new Set(CATS.map(c=>c.key));
let currentData = null;

// Returns the subset of currentData.categories that are currently selected.
// This is the single source of truth for all category-dependent UI rendering.
function activeCategories() {
  if (!currentData || !currentData.categories) return [];
  return currentData.categories.filter(c => activeCats.has(c.key));
}

function renderCatDropdown(keepOpen = false){
  const container = document.getElementById('catDropdown');
  if(!container) return;
  container.innerHTML = '';

  const selectedList = CATS.filter(c => activeCats.has(c.key));

  let triggerChipsHtml = '';
  if (selectedList.length === 0) {
    triggerChipsHtml = '<span style="color:var(--muted)">Select categories...</span>';
  } else if (selectedList.length <= 4) {
    triggerChipsHtml = selectedList.map(c => `
      <span class="badge-chip">
        <span class="swatch" style="display:inline-block;width:7px;height:7px;border-radius:2px;background:${c.color}"></span>
        ${c.label}
      </span>
    `).join('');
  } else {
    const visibleChips = selectedList.slice(0, 3).map(c => `
      <span class="badge-chip">
        <span class="swatch" style="display:inline-block;width:7px;height:7px;border-radius:2px;background:${c.color}"></span>
        ${c.label}
      </span>
    `).join('');
    const extraCount = selectedList.length - 3;
    triggerChipsHtml = visibleChips + `<span class="badge-chip">+${extraCount} more</span>`;
  }

  const dropdownHtml = `
    <div class="dropdown-trigger" id="dropdownTrigger" tabindex="0" role="button" aria-haspopup="true" aria-expanded="false">
      <div class="dropdown-chips">
        <strong style="margin-right:6px;font-size:12.5px;">Categories (${selectedList.length}/${CATS.length}):</strong>
        ${triggerChipsHtml}
      </div>
      <span class="dropdown-arrow" style="font-size:10px;color:var(--muted);">▼</span>
    </div>
    <div class="dropdown-popover" id="dropdownPopover" role="menu">
      <div class="dropdown-header-actions">
        <span style="font-size:11.5px;font-weight:700;color:var(--text);">Filter Categories (30 each)</span>
        <div>
          <button class="action-btn-sm" id="selectAllCats">Select All</button>
          <button class="action-btn-sm" id="clearAllCats">Clear All</button>
        </div>
      </div>
      <div class="dropdown-options-grid">
        ${CATS.map(c => `
          <label class="dropdown-option-item">
            <input type="checkbox" value="${c.key}" ${activeCats.has(c.key) ? 'checked' : ''}>
            <span class="swatch" style="display:inline-block;width:8px;height:8px;border-radius:2px;background:${c.color}"></span>
            <span>${c.label}</span>
          </label>
        `).join('')}
      </div>
    </div>
  `;

  container.innerHTML = dropdownHtml;

  const trigger = container.querySelector('#dropdownTrigger');
  const popover = container.querySelector('#dropdownPopover');
  const arrow = container.querySelector('.dropdown-arrow');

  const togglePopover = (open) => {
    const isOpen = open !== undefined ? open : !popover.classList.contains('open');
    popover.classList.toggle('open', isOpen);
    trigger.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
    arrow.textContent = isOpen ? '▲' : '▼';
  };

  if (keepOpen) {
    togglePopover(true);
  }

  trigger.onclick = (e) => {
    e.stopPropagation();
    togglePopover();
  };

  trigger.onkeydown = (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      togglePopover();
    } else if (e.key === 'Escape') {
      togglePopover(false);
    }
  };

  container.querySelector('#selectAllCats').onclick = (e) => {
    e.stopPropagation();
    CATS.forEach(c => activeCats.add(c.key));
    renderCatDropdown(true);
    if(currentData) { renderUI(); scheduleAutoGenerate(); }
  };

  container.querySelector('#clearAllCats').onclick = (e) => {
    e.stopPropagation();
    activeCats.clear();
    renderCatDropdown(true);
    if(currentData) { renderUI(); scheduleAutoGenerate(); }
  };

  popover.querySelectorAll('input[type="checkbox"]').forEach(chk => {
    chk.onclick = (e) => {
      e.stopPropagation();
      const key = chk.value;
      if (chk.checked) activeCats.add(key);
      else activeCats.delete(key);
      renderCatDropdown(true);
      if(currentData) { renderUI(); scheduleAutoGenerate(); }
    };
  });

  if (!window.__catDropdownOutsideBound) {
    window.__catDropdownOutsideBound = true;
    document.addEventListener('click', (e) => {
      const dropdownEl = document.getElementById('catDropdown');
      if (dropdownEl && !dropdownEl.contains(e.target)) {
        const pop = dropdownEl.querySelector('#dropdownPopover');
        const trg = dropdownEl.querySelector('#dropdownTrigger');
        const arw = dropdownEl.querySelector('.dropdown-arrow');
        if (pop && pop.classList.contains('open')) {
          pop.classList.remove('open');
          if(trg) trg.setAttribute('aria-expanded', 'false');
          if(arw) arw.textContent = '▼';
        }
      }
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        const dropdownEl = document.getElementById('catDropdown');
        if (dropdownEl) {
          const pop = dropdownEl.querySelector('#dropdownPopover');
          const trg = dropdownEl.querySelector('#dropdownTrigger');
          const arw = dropdownEl.querySelector('.dropdown-arrow');
          if (pop && pop.classList.contains('open')) {
            pop.classList.remove('open');
            if(trg) trg.setAttribute('aria-expanded', 'false');
            if(arw) arw.textContent = '▼';
          }
        }
      }
    });
  }
}

function currentChannelName(){
  const el = document.getElementById('channelInput');
  return (el && el.value.trim()) || 'TechNova IQ';
}

function currentTopic(){
  const el = document.getElementById('topicInput');
  return (el && el.value.trim()) || 'SQL Server Performance Tuning';
}

function currentPrompt(){
  const el = document.getElementById('promptArea');
  return (el && el.value.trim()) || '';
}

let promptData = null;

async function fetchStudioData(){
  const channelName = currentChannelName();
  const videoTopic = currentTopic();
  const selectedCategories = Array.from(activeCats);

  try {
    const res = await fetch('/api/tags/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ channelName, videoTopic, selectedCategories, prompt: '' })
    });
    if(!res.ok) throw new Error('API server error');
    currentData = await res.json();
    renderUI();
  } catch (err) {
    console.error('Error fetching tags:', err);
    showToast('Failed to connect to Spring Boot backend');
  }
}

let promptHistory = [];

function cleanTopicString(str) {
  if (!str) return '';
  let s = str.trim();
  // Strip emojis and decorative symbols (like 📌, etc.)
  s = s.replace(/[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}📌*#_~`"']/gu, ' ').trim();
  // Strip prefixes like "The Topic:", "Topic:", "the topic", "my topic:", etc.
  s = s.replace(/^(?:the\s+|my\s+)?topic\s*(?:is|:|=|-|\s)\s*/i, '');
  s = s.replace(/^(?:the\s+)?topic\b\s*/i, '');
  s = s.replace(/[*#_~`"':\-]/g, ' ').trim();
  // Strip common trailing instruction phrases
  s = s.replace(/\s+(?:please|and\s+focus|focusing|focused|with|in\s+2026).*$/i, '').trim();
  s = s.replace(/\s+/g, ' ').trim();
  if (!s) return '';
  return s.replace(/\b\w+/g, w => {
    if (w.toLowerCase() === 'devops') return 'DevOps';
    if (w.toLowerCase() === 'sql') return 'SQL';
    if (w.toLowerCase() === 'api') return 'API';
    if (w.toLowerCase() === 'aws') return 'AWS';
    return w.charAt(0).toUpperCase() + w.slice(1);
  });
}

function formatYouTubeDescription(rawText) {
  if (!rawText) return '';
  let text = String(rawText);

  // Strip "Studio AI" / "Studio AI:" prefix if present
  text = text.replace(/^\s*(?:<strong>)?Studio AI(?:<\/strong>)?\s*[:\-]?\s*/i, '');

  // Convert literal \n and \r escape sequences to real newlines
  text = text.replace(/\\r\\n/g, '\n').replace(/\\n/g, '\n').replace(/\\r/g, '\n');

  // Convert HTML break tags to newlines
  text = text.replace(/<br\s*\/?>/gi, '\n');

  // Decode common HTML entities safely
  text = text.replace(/&amp;/g, '&')
             .replace(/&quot;/g, '"')
             .replace(/&#39;/g, "'")
             .replace(/&lt;/g, '<')
             .replace(/&gt;/g, '>')
             .replace(/&nbsp;/g, ' ')
             .replace(/&#x20;/g, ' ')
             .replace(/&#32;/g, ' ');

  // Strip HTML tags
  text = text.replace(/<[^>]+>/g, '');

  // Strip Markdown headers (# Title, ## Description, etc.) if present as standalone header lines
  text = text.replace(/^#{1,6}\s+(?:Title|Description)?\s*$/gmi, '');

  // Strip markdown bolding **text** -> text, while preserving bullet points
  text = text.replace(/\*\*(.*?)\*\*/g, '$1');

  // Normalize line breaks
  text = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  // YouTube Shorts whitespace collapsing fix:
  // Replace each empty or whitespace-only line with invisible Unicode zero-width space (\u200B)
  const lines = text.split('\n');
  const result = lines.map(line => line.trim().length === 0 ? '\u200B' : line.trim());
  return result.join('\n').trim();
}

function optimizeDescriptionTemplate(rawTemplate, topic, channel) {
  if (!rawTemplate) return '';
  const ch = channel || currentChannelName() || 'TechNova IQ';
  const primaryTopic = topic ? cleanTopicString(topic) : (currentTopic() ? cleanTopicString(currentTopic()) : 'Azure DevOps Pipeline Optimization');

  // If already structured with "What you'll learn", return as-is
  if (rawTemplate.includes("What you'll learn") && rawTemplate.includes("Key takeaway")) {
    return rawTemplate;
  }

  // Parse sections by double newlines
  const paras = rawTemplate.split(/\n\n+/);
  let hook = '';
  let keywordsRaw = '';
  let audienceRaw = '';
  let question = '';
  let closingCta = '';

  for (const p of paras) {
    const trimmed = p.trim();
    if (trimmed.startsWith('Learn how') || trimmed.startsWith('Is your') || trimmed.startsWith('Discover how') || (!hook && !trimmed.includes("walkthrough, you'll explore") && !trimmed.includes('Perfect for') && !trimmed.startsWith('#') && !trimmed.includes('— Learn Smarter'))) {
      hook = trimmed;
    } else if (trimmed.includes("walkthrough, you'll explore") || trimmed.includes('explore ') || trimmed.includes('master essential techniques')) {
      keywordsRaw = trimmed;
    } else if (trimmed.startsWith('Perfect for') || trimmed.toLowerCase().includes('beginners, developers')) {
      audienceRaw = trimmed;
    } else if (trimmed.startsWith('What ') || trimmed.includes('Share below') || trimmed.includes('Which stage takes the longest')) {
      question = trimmed;
    } else if (trimmed.startsWith('If this helped') || trimmed.startsWith('Like this video') || trimmed.includes('subscribe to')) {
      closingCta = trimmed;
    }
  }

  if (!hook) {
    hook = `Master ${primaryTopic} with practical, real-world walkthroughs from ${ch} to accelerate your build performance and engineering workflow! 🚀`;
  }

  const explanation = `In this video, learn practical approaches to ${primaryTopic}, focusing on identifying pipeline bottlenecks, optimizing dependencies, and streamlining execution for reliable continuous integration.`;

  const bullets = [];
  if (keywordsRaw) {
    const itemsOnly = keywordsRaw.replace(/^.*?explore\s+/i, '').replace(/,\s*helping you master.*$/i, '');
    const rawItems = itemsOnly.split(/,\s*/);
    const seen = new Set();
    const topicRegex = new RegExp('^' + primaryTopic.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*', 'i');
    for (const item of rawItems) {
      let cleanItem = item.trim().replace(topicRegex, '').trim();
      if (!cleanItem || /(?:2026|full course free|basics|tutorial|explained)/i.test(cleanItem)
          || /^(?:database design|database normalization|clean code|coding best practices|YouTube Shorts|Shorts tutorial)$/i.test(cleanItem)) {
        continue;
      }
      cleanItem = cleanItem.charAt(0).toUpperCase() + cleanItem.slice(1);
      if (!seen.has(cleanItem.toLowerCase())) {
        seen.add(cleanItem.toLowerCase());
        bullets.push(`- ${cleanItem} best practices and techniques.`);
        if (bullets.length >= 5) break;
      }
    }
  }
  if (bullets.length === 0) {
    bullets.push('- Identify and resolve pipeline bottlenecks efficiently.');
    bullets.push('- Configure dependency caching to eliminate repeated downloads.');
    bullets.push('- Parallelize independent jobs across available agent capacity.');
    bullets.push('- Measure performance benchmarks before and after optimization.');
  }

  const keyTakeaway = `**Key takeaway:** Measure pipeline performance first, eliminate redundant downloads through caching, and execute independent jobs in parallel for maximum build efficiency.`;

  let targetAudience = '**Target audience:** Developers, DevOps engineers, cloud architects, and technical interview candidates.';
  if (audienceRaw) {
    let cleanAudience = audienceRaw
      .replace(/^(?:Perfect for|Whether you are a|Whether you're a)\s*/i, '')
      .replace(/(?:,\s*this breakdown is designed for you\.?|,\s*this guide is for you\.?|\.?)$/i, '')
      .trim();
    const topicRegex = new RegExp('^' + primaryTopic.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*', 'i');
    cleanAudience = cleanAudience.replace(topicRegex, '').replace(/^[\s,]+/, '').trim();
    if (cleanAudience) {
      cleanAudience = cleanAudience.charAt(0).toUpperCase() + cleanAudience.slice(1);
      targetAudience = `**Target audience:** ${cleanAudience}${cleanAudience.endsWith('.') ? '' : '.'}`;
    }
  }

  if (!question) {
    question = `💬 Which pipeline stage takes the longest in your workflow: Restore, Build, or Test? Share your experience in the comments below!`;
  } else if (!question.startsWith('💬')) {
    question = `💬 ${question}`;
  }

  if (!closingCta) {
    closingCta = `👍 Like this video and subscribe to **${ch}** for practical DevOps tutorials, cloud engineering, and technical interview preparation.`;
  } else if (!closingCta.startsWith('👍')) {
    closingCta = `👍 ${closingCta}`;
  }

  const brandClosing = `${ch} — Learn Smarter. Code Faster.`;

  const hashtags = new Set();
  hashtags.add('#' + primaryTopic.replace(/[^a-zA-Z0-9]/g, ''));
  hashtags.add('#CICD');
  hashtags.add('#DevOps');
  hashtags.add('#PipelineOptimization');
  hashtags.add('#CloudEngineering');
  hashtags.add('#' + ch.replace(/[^a-zA-Z0-9]/g, ''));
  hashtags.add('#Shorts');
  const hashtagLine = Array.from(hashtags).join(' ');

  return [
    hook,
    explanation,
    `🔹 **What you'll learn:**\n${bullets.join('\n')}`,
    keyTakeaway,
    targetAudience,
    question,
    closingCta,
    brandClosing,
    hashtagLine
  ].join('\n\n');
}

function extractTopicFromPrompt(promptText) {
  if (!promptText || !promptText.trim()) return null;
  const p = promptText.trim();
  const patterns = [
    /(?:^|\n)\s*(?:the\s+|my\s+)?topic\s*[:=]\s*["']?([^"'\n\r]+)["']?/i,
    /(?:my\s+|the\s+)?topic\s+(?:is|:|=)\s+([^.\n\r,;]+)/i,
    /(?:create|generate|provide|write|make)\s+(?:the\s+)?(?:youtube\s+)?(?:metadata|tags|keywords|titles|hashtags|description|content|video)\s+(?:for|about|on)\s+(?:(?:the\s+)?topic\s*[:=]?\s*)?([^.\n\r,;]+)/i,
    /(?:video|metadata|tags|tutorial|guide)\s+(?:for|about|on)\s+(?:(?:the\s+)?topic\s*[:=]?\s*)?([^.\n\r,;]+)/i
  ];

  for (const pattern of patterns) {
    const m = p.match(pattern);
    if (m && m[1]) {
      const candidate = cleanTopicString(m[1]);
      if (candidate) {
        return candidate;
      }
    }
  }

  // If the prompt is a single line / direct topic input (e.g. "Azure DevOps Interview Questions" or "The Topic: Azure DevOps Interview Questions")
  if (p.length < 100 && !p.includes('\n')) {
    const candidate = cleanTopicString(p);
    if (candidate) {
      return candidate;
    }
  }

  return null;
}

async function generatePromptResult(){
  const channelName = currentChannelName();
  const rawPrompt = currentPrompt();
  if (!rawPrompt || !rawPrompt.trim()) {
    showToast('Please enter a prompt');
    return;
  }

  const extractedTopic = extractTopicFromPrompt(rawPrompt);
  const topicInputEl = document.getElementById('topicInput');
  const fallbackTopic = topicInputEl ? cleanTopicString(topicInputEl.value) : '';
  const videoTopic = extractedTopic || fallbackTopic || 'SQL Server Performance Tuning';
  const prompt = rawPrompt;
  const selectedCategories = Array.from(activeCats);

  try {
    const res = await fetch('/api/tags/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ channelName, videoTopic, selectedCategories, prompt })
    });
    if(!res.ok) throw new Error('API server error');
    const data = await res.json();
    
    const cleanTopic = cleanTopicString(data.videoTopic || videoTopic);
    // Add new history entry for this specific topic and prompt
    const entry = {
      id: 'ph_' + Date.now(),
      topic: cleanTopic,
      prompt: prompt,
      data: data,
      timestamp: new Date().toLocaleTimeString()
    };
    promptHistory.unshift(entry);
    renderPromptHistory();
    showToast('Generated for: ' + cleanTopic);
  } catch (err) {
    console.error('Error generating prompt:', err);
    showToast('Failed to generate prompt');
  }
}

function renderPromptHistory(){
  const container = document.getElementById('promptResults');
  if(!container) return;
  container.innerHTML = '';

  if(promptHistory.length === 0){
    return;
  }

  promptHistory.forEach((entry, idx) => {
    const entryCard = document.createElement('div');
    entryCard.className = 'prompt-history-entry';
    entryCard.style.cssText = 'background:var(--panel);border:1px solid var(--border);border-radius:8px;padding:16px;margin-bottom:20px;box-shadow:var(--shadow);';

    const header = document.createElement('div');
    header.style.cssText = 'display:flex;align-items:center;justify-content:space-between;margin-bottom:14px;padding-bottom:10px;border-bottom:1px solid var(--border);flex-wrap:wrap;gap:8px;';
    header.innerHTML = `
      <div style="display:flex;align-items:center;gap:8px;">
        <span style="font-size:14px;font-weight:700;color:var(--text);">${entry.topic}</span>
      </div>
      <div style="font-size:11.5px;color:var(--muted);">${entry.timestamp}${entry.prompt ? ' · Prompt: ' + (entry.prompt.length > 40 ? entry.prompt.slice(0,37) + '...' : entry.prompt) : ''}</div>
    `;
    entryCard.appendChild(header);

    const contentContainer = document.createElement('div');
    renderPromptContent(entry.data, contentContainer, entry.id);
    entryCard.appendChild(contentContainer);

    container.appendChild(entryCard);
  });
}

function renderPromptContent(data, container, entryId){
  if(!container || !data) return;

  const meta = data.metadata;
  const cats = (data.categories || []).filter(c => activeCats.has(c.key));

  if(meta) {
    const metaHeaderGrid = document.createElement('div');
    metaHeaderGrid.className = 'metadata-grid';
    metaHeaderGrid.style.marginBottom = '20px';

    // Title Options Block
    const titleBlock = document.createElement('div');
    titleBlock.className = 'metablock';
    titleBlock.innerHTML = `<div class="metablock-head"><span>Title options (Prompt)</span><span style="color:var(--muted);font-weight:400;font-size:11px;">100 char limit</span></div><div class="metablock-body" id="${entryId}_promptTitleList"></div>`;
    metaHeaderGrid.appendChild(titleBlock);
    const titleList = titleBlock.querySelector('#' + entryId + '_promptTitleList');
    (meta.titleOptions || []).forEach(t => {
      const row = document.createElement('div');
      row.className = 'titleopt';
      const over = t.length > 100;
      row.innerHTML = `<span>${t}</span><span class="len" style="color:${over?'var(--rose)':'var(--muted)'}">${t.length}/100</span>`;
      row.style.cursor = 'pointer';
      row.onclick = () => safeCopy(t, 'Title copied');
      titleList.appendChild(row);
    });

    // Description Template Block
    const descAreaId = entryId + '_promptDescArea';
    const descBlock = document.createElement('div');
    descBlock.className = 'metablock';
    const optimizedPromptDesc = optimizeDescriptionTemplate(meta.descriptionTemplate, cleanTopic, currentChannelName());
    descBlock.innerHTML = `<div class="metablock-head"><span>Description template</span><button class="copybtn" id="${entryId}_copyPromptDesc">copy</button></div><div class="metablock-body"><textarea id="${descAreaId}" style="min-height:140px;">${optimizedPromptDesc || ''}</textarea><div class="charmeter">Generated from prompt instructions &amp; selected categories.</div></div>`;
    metaHeaderGrid.appendChild(descBlock);
    descBlock.querySelector('#' + entryId + '_copyPromptDesc').onclick = (e) => {
      e.stopPropagation();
      safeCopy(formatYouTubeDescription(document.getElementById(descAreaId).value), 'Description copied');
    };

    container.appendChild(metaHeaderGrid);

    // 3️⃣ Call to Action (CTA) Block
    const ctaAreaId = entryId + '_promptCtaArea';
    const ctaCopyBtnId = entryId + '_copyPromptCta';
    const ctaText = (meta && meta.ctaText) ||
      (data.playlistMetadata && data.playlistMetadata.ctaText) ||
      `👍 Like the video if this was helpful.\n` +
      `🔔 Subscribe to ${currentChannelName()} for more practical ${cleanTopicString(data.videoTopic || '')} tutorials and Shorts.\n` +
      `💬 Drop your questions and doubts in the comments below!`;

    createAccordionCardHelper({
      id: entryId + '_promptCtaBlock',
      title: '3️⃣ Call to Action (CTA)',
      copyButtons: [{
        btnId: ctaCopyBtnId,
        label: 'copy',
        action: () => safeCopy(document.getElementById(ctaAreaId).value, 'CTA copied')
      }],
      contentHtml: `
        <textarea id="${ctaAreaId}" style="min-height:75px;">${ctaText}</textarea>
        <div class="charmeter">Use at the end of your description or pin as a comment.</div>
      `,
      container: container
    });

    // Tags Fields
    if(cats.length > 0) {
      const tagsSectionHead = document.createElement('h3');
      tagsSectionHead.className = 'sectionhead';
      tagsSectionHead.textContent = '🏷️ Category Tags Fields (500-char limit)';
      container.appendChild(tagsSectionHead);

      const tagsGrid = document.createElement('div');
      tagsGrid.className = 'masonry-grid';
      const tagsMap = meta.categoryTagsFields || {};

      cats.forEach(cat => {
        const str = tagsMap[cat.key] || '';
        const cnt = str ? str.split(',').filter(s => s.trim()).length : 0;
        const areaId = entryId + '_tags_' + cat.key;
        const block = document.createElement('div');
        block.className = 'catblock';
        block.innerHTML = `
          <div class="catblock-head" tabindex="0" role="button" aria-expanded="true">
            <div class="catblock-head-left">
              <span class="swatch" style="background:${cat.color}"></span>
              <span class="name">${cat.label} tags field</span>
            </div>
            <div class="catblock-head-right">
              <button class="copybtn" id="copy_${areaId}">copy</button>
            </div>
          </div>
          <div class="card-full" style="display:block;padding:12px 14px;">
            <textarea id="${areaId}" style="min-height:75px;">${str}</textarea>
            <div class="charmeter">${str.length}/500 chars · ${cnt}/30 tags</div>
          </div>
        `;
        block.querySelector('#copy_' + areaId).onclick = (e) => {
          e.stopPropagation();
          safeCopy(document.getElementById(areaId).value, cat.label + ' tags copied');
        };
        tagsGrid.appendChild(block);
      });
      container.appendChild(tagsGrid);
    }
  }

  // Hashtags & Keywords
  if(cats.length > 0) {
    const hashHead = document.createElement('h3');
    hashHead.className = 'sectionhead';
    hashHead.style.marginTop = '24px';
    hashHead.textContent = '🏷️ Generated Hashtags';
    container.appendChild(hashHead);

    const hashGrid = document.createElement('div');
    hashGrid.className = 'masonry-grid';
    cats.forEach(cat => {
      const block = document.createElement('div');
      block.className = 'catblock';
      const hashes = cat.hashtags || [];
      const btnId = entryId + '_copyHash_' + cat.key;
      block.innerHTML = `
        <div class="catblock-head">
          <div class="catblock-head-left">
            <span class="swatch" style="background:${cat.color}"></span>
            <span class="name">${cat.label}</span>
            <span class="cnt">(${hashes.length})</span>
          </div>
          <div class="catblock-head-right">
            <button class="copybtn" id="${btnId}">copy all</button>
          </div>
        </div>
        <div class="card-full" style="display:block;padding:12px 14px;">
          <div class="taglist" style="padding:0;">${hashes.map(h => `<span class="tag"><span class="taglabel">${h}</span></span>`).join('')}</div>
        </div>
      `;
      block.querySelector('#' + btnId).onclick = (e) => {
        e.stopPropagation();
        safeCopy(hashes.join(' '), `${cat.label} hashtags copied`);
      };
      block.querySelectorAll('.tag').forEach(tagEl => {
        tagEl.onclick = (e) => {
          e.stopPropagation();
          safeCopy(tagEl.querySelector('.taglabel').textContent, 'Tag copied');
        };
      });
      hashGrid.appendChild(block);
    });
    container.appendChild(hashGrid);

    const kwHead = document.createElement('h3');
    kwHead.className = 'sectionhead';
    kwHead.style.marginTop = '24px';
    kwHead.textContent = '🔑 Generated Keywords';
    container.appendChild(kwHead);

    const kwGrid = document.createElement('div');
    kwGrid.className = 'masonry-grid';
    cats.forEach(cat => {
      const block = document.createElement('div');
      block.className = 'catblock';
      const kws = cat.keywords || [];
      const btnId = entryId + '_copyKw_' + cat.key;
      const kwAreaId = entryId + '_kwArea_' + cat.key;
      block.innerHTML = `
        <div class="catblock-head">
          <div class="catblock-head-left">
            <span class="swatch" style="background:${cat.color}"></span>
            <span class="name">${cat.label}</span>
            <span class="cnt">(${kws.length})</span>
          </div>
          <div class="catblock-head-right">
            <button class="copybtn" id="${btnId}">copy all</button>
          </div>
        </div>
        <div class="card-full" style="display:block;padding:12px 14px;">
          <textarea id="${kwAreaId}" style="min-height:85px;">${kws.join(', ')}</textarea>
        </div>
      `;
      block.querySelector('#' + btnId).onclick = (e) => {
        e.stopPropagation();
        safeCopy(document.getElementById(kwAreaId).value, `${cat.label} keywords copied`);
      };
      kwGrid.appendChild(block);
    });
    container.appendChild(kwGrid);
  }
}

function renderListTab(container, isHashtagMode){
  if(!container || !currentData) return;
  const sep = isHashtagMode ? ' ' : ', ';
  container.innerHTML = '';

  const activeCategoriesList = activeCategories();
  if(activeCategoriesList.length === 0){
    container.innerHTML = '<p style="color:var(--muted);font-family:IBM Plex Mono,monospace;font-size:13px;">No categories selected.</p>';
    return;
  }

  const masonryGrid = document.createElement('div');
  masonryGrid.className = 'masonry-grid';

  activeCategoriesList.forEach(cat=>{
    const items = isHashtagMode ? cat.hashtags : cat.keywords;
    const rawItems = cat.rawPhrases;
    const totalCount = items.length;
    const previewCount = Math.min(4, totalCount);
    const remainingCount = totalCount - previewCount;

    const block = document.createElement('div');
    block.className = 'catblock';
    
    // Preview items (first 4)
    const previewItemsHtml = items.slice(0, previewCount).map((t,i)=>`
      <span class="tag" data-raw="${rawItems[i].replace(/"/g,'&quot;')}">
        <span class="taglabel">${t}</span>
        <button class="checkbtn" title="Check live YouTube activity">⟳</button>
        <span class="activitybadge"></span>
      </span>
    `).join('');

    const moreBadgeHtml = remainingCount > 0 
      ? `<button class="more-badge" title="Expand category">+${remainingCount} more</button>` 
      : '';

    // Full items (all items)
    const fullItemsHtml = items.map((t,i)=>`
      <span class="tag" data-raw="${rawItems[i].replace(/"/g,'&quot;')}">
        <span class="taglabel">${t}</span>
        <button class="checkbtn" title="Check live YouTube activity">⟳</button>
        <span class="activitybadge"></span>
      </span>
    `).join('');

    block.innerHTML = `
      <div class="catblock-head" tabindex="0" role="button" aria-expanded="false" title="Click to expand/collapse">
        <div class="catblock-head-left">
          <span class="swatch" style="background:${cat.color}"></span>
          <span class="name">${cat.label}</span>
          <span class="cnt">(${totalCount})</span>
        </div>
        <div class="catblock-head-right">
          <button class="copybtn" title="Copy all ${totalCount} items">copy all ${totalCount}</button>
          <button class="expand-toggle" aria-label="Toggle category expand">▼</button>
        </div>
      </div>
      <div class="card-preview">
        ${previewItemsHtml}
        ${moreBadgeHtml}
      </div>
      <div class="card-full">
        <div class="taglist" style="padding:0;">${fullItemsHtml}</div>
      </div>
    `;

    // Toggle expand function
    const headEl = block.querySelector('.catblock-head');
    const toggleBtn = block.querySelector('.expand-toggle');
    const moreBadge = block.querySelector('.more-badge');

    const toggleExpand = (e)=>{
      if (e) e.stopPropagation();
      const isExpanded = block.classList.toggle('expanded');
      headEl.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
      toggleBtn.textContent = isExpanded ? '▲' : '▼';
    };

    headEl.onclick = (e)=> toggleExpand(e);
    headEl.onkeydown = (e)=>{
      if(e.key === 'Enter' || e.key === ' '){
        e.preventDefault();
        toggleExpand(e);
      }
    };
    if(moreBadge){
      moreBadge.onclick = (e)=> toggleExpand(e);
    }

    // Copy all button (with stopPropagation)
    const copyBtn = block.querySelector('.copybtn');
    copyBtn.onclick = (e)=>{
      e.stopPropagation();
      safeCopy(items.join(sep), `${cat.label} copied (${totalCount})`);
    };

    // Tag click & checkbtn activity check (with stopPropagation)
    block.querySelectorAll('.tag').forEach(tagEl=>{
      const btn = tagEl.querySelector('.checkbtn');
      const badge = tagEl.querySelector('.activitybadge');
      tagEl.onclick = (e)=>{
        e.stopPropagation();
        safeCopy(tagEl.querySelector('.taglabel').textContent, 'Tag copied');
      };
      btn.onclick = async (e)=>{
        e.stopPropagation();
        badge.textContent = '...';
        badge.className = 'activitybadge';
        try{
          const res = await fetch('/api/youtube/activity?q=' + encodeURIComponent(tagEl.dataset.raw));
          const data = await res.json();
          if(data.error || data.status === 'error'){
            badge.textContent = 'unavailable';
            badge.className = 'activitybadge err';
          } else {
            badge.textContent = `~${data.avgViews.toLocaleString()} avg views`;
            badge.className = 'activitybadge';
          }
        } catch(err){
          badge.textContent = 'offline';
          badge.className = 'activitybadge err';
        }
      };
    });

    masonryGrid.appendChild(block);
  });

  container.appendChild(masonryGrid);

  const totalCount = activeCategoriesList.reduce((acc, c) => acc + (isHashtagMode ? c.hashtags.length : c.keywords.length), 0);
  const copyAllBtn = document.createElement('button');
  copyAllBtn.className = 'copyall';
  copyAllBtn.textContent = `Copy everything in this tab (${totalCount} items)`;
  copyAllBtn.onclick = (e)=>{
    e.stopPropagation();
    const allItems = activeCategoriesList.flatMap(c => isHashtagMode ? c.hashtags : c.keywords);
    safeCopy(allItems.join(sep), allItems.length + ' items copied');
  };
  container.appendChild(copyAllBtn);
}

// Helper to build a standard accordion card identical across tabs
function createAccordionCardHelper({ id, title, color, copyButtons, contentHtml, container }) {
  const block = document.createElement('div');
  block.className = 'catblock';
  if (id) block.id = id;

  const copyBtnsHtml = (copyButtons || []).map(b =>
    `<button class="copybtn" id="${b.btnId}">${b.label}</button>`
  ).join('');

  const swatchHtml = color ? `<span class="swatch" style="background:${color}"></span>` : '';

  block.innerHTML = `
    <div class="catblock-head" tabindex="0" role="button" aria-expanded="false" title="Click to expand/collapse">
      <div class="catblock-head-left">
        ${swatchHtml}
        <span class="name">${title}</span>
      </div>
      <div class="catblock-head-right">
        ${copyBtnsHtml}
        <button class="expand-toggle" aria-label="Toggle section">▼</button>
      </div>
    </div>
    <div class="card-full" style="padding:12px 14px;">
      ${contentHtml}
    </div>
  `;

  const headEl  = block.querySelector('.catblock-head');
  const toggleBtn = block.querySelector('.expand-toggle');
  const toggleExpand = (e) => {
    if (e) e.stopPropagation();
    const isExpanded = block.classList.toggle('expanded');
    headEl.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
    toggleBtn.textContent = isExpanded ? '▲' : '▼';
  };

  headEl.onclick   = (e) => toggleExpand(e);
  headEl.onkeydown = (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      toggleExpand(e);
    }
  };

  (copyButtons || []).forEach(b => {
    const btn = block.querySelector('#' + b.btnId);
    if (btn) {
      btn.onclick = (e) => {
        e.stopPropagation();
        b.action();
      };
    }
  });

  block.querySelectorAll('.tag').forEach(tagEl => {
    tagEl.onclick = (e) => {
      e.stopPropagation();
      const lbl = tagEl.querySelector('.taglabel');
      if (lbl) safeCopy(lbl.textContent, 'Copied');
    };
  });

  if (container) {
    container.appendChild(block);
  }
  return block;
}

function buildMetadataTab(container){
  if(!container || !currentData || !currentData.metadata) return;
  container.innerHTML = '';
  const meta = currentData.metadata;

  // Top Header Grid (Title options & Description template)
  const metaHeaderGrid = document.createElement('div');
  metaHeaderGrid.className = 'metadata-grid';
  metaHeaderGrid.style.marginBottom = '20px';

  // Title Options Block
  const titleBlock = document.createElement('div');
  titleBlock.className = 'metablock';
  titleBlock.innerHTML = `<div class="metablock-head"><span>Title options</span><span style="color:var(--muted);font-weight:400;font-size:11px;">100 char limit</span></div><div class="metablock-body" id="titleList"></div>`;
  metaHeaderGrid.appendChild(titleBlock);
  const titleList = titleBlock.querySelector('#titleList');
  (meta.titleOptions || []).forEach(t=>{
    const row = document.createElement('div');
    row.className = 'titleopt';
    const over = t.length > 100;
    row.innerHTML = `<span>${t}</span><span class="len" style="color:${over?'var(--rose)':'var(--muted)'}">${t.length}/100</span>`;
    row.style.cursor='pointer';
    row.onclick = ()=>{ safeCopy(t, 'Title copied'); };
    titleList.appendChild(row);
  });

  // Description Template Block
  const descBlock = document.createElement('div');
  descBlock.className = 'metablock';
  const optimizedDesc = optimizeDescriptionTemplate(meta.descriptionTemplate, currentTopic(), currentChannelName());
  descBlock.innerHTML = `<div class="metablock-head"><span>Description template</span><button class="copybtn" id="copyDesc">copy</button></div><div class="metablock-body"><textarea id="descArea" style="min-height:140px;">${optimizedDesc}</textarea><div class="charmeter">Only the first 3 hashtags placed in a description show above the video title — rest still work for search, just not visibly.</div></div>`;
  metaHeaderGrid.appendChild(descBlock);
  descBlock.querySelector('#copyDesc').onclick = (e)=>{
    e.stopPropagation();
    safeCopy(formatYouTubeDescription(document.getElementById('descArea').value), 'Description copied');
  };

  container.appendChild(metaHeaderGrid);

  // 3️⃣ Call to Action (CTA)
  const ctaText = (meta && meta.ctaText) || (currentData.playlistMetadata && currentData.playlistMetadata.ctaText) || '';
  createAccordionCardHelper({
    id: 'videoCta',
    title: '3️⃣ Call to Action (CTA)',
    copyButtons: [{
      btnId: 'copyVideoCta',
      label: 'copy',
      action: () => safeCopy(document.getElementById('videoCtaArea').value, 'CTA copied')
    }],
    contentHtml: `
      <textarea id="videoCtaArea" style="min-height:75px;">${ctaText}</textarea>
    `,
    container: container
  });

  // Section Header for Tags Fields
  const tagsSectionHead = document.createElement('h3');
  tagsSectionHead.className = 'sectionhead';
  tagsSectionHead.textContent = '🏷️ Category Tags Fields (500-char limit)';
  container.appendChild(tagsSectionHead);

  // Category Tags Fields (Masonry Cards)
  const tagsMap = meta.categoryTagsFields || {};
  const masonryGrid = document.createElement('div');
  masonryGrid.className = 'masonry-grid';

  const selectedCats = activeCategories();
  if (selectedCats.length === 0) {
    const emptyMsg = document.createElement('p');
    emptyMsg.style.cssText = 'color:var(--muted);font-family:IBM Plex Mono,monospace;font-size:13px;margin-top:8px;';
    emptyMsg.textContent = 'No categories selected.';
    container.appendChild(emptyMsg);
    return;
  }

  selectedCats.forEach(cat => {
    const str = tagsMap[cat.key] || '';
    const cnt = str ? str.split(',').filter(s => s.trim()).length : 0;
    const areaId = 'tagsArea_' + cat.key;
    const meterId = 'meter_' + cat.key;

    const previewStr = str.length > 90 ? str.slice(0, 90) + '...' : str;

    const block = document.createElement('div');
    block.className = 'catblock';
    block.innerHTML = `
      <div class="catblock-head" tabindex="0" role="button" aria-expanded="false" title="Click to expand/collapse tags field">
        <div class="catblock-head-left">
          <span class="swatch" style="background:${cat.color}"></span>
          <span class="name">${cat.label} tags field</span>
        </div>
        <div class="catblock-head-right">
          <button class="copybtn" id="copy_${areaId}">copy</button>
          <button class="expand-toggle" aria-label="Toggle tags field expand">▼</button>
        </div>
      </div>
      <div class="card-preview">
        <div style="font-family:'IBM Plex Mono',monospace;font-size:12px;color:var(--text);width:100%;margin-bottom:6px;word-break:break-word;">
          ${previewStr || '<span style="color:var(--muted)">No tags</span>'}
        </div>
        <div class="charmeter" style="width:100%;margin-top:0;">
          <span>${str.length}</span>/500 chars &nbsp;·&nbsp; <span>${cnt}</span>/30 tags fit
        </div>
      </div>
      <div class="card-full">
        <textarea id="${areaId}" style="min-height:90px;">${str}</textarea>
        <div class="charmeter" id="${meterId}">
          <span id="${meterId}_chars">${str.length}</span>/500 characters &nbsp;·&nbsp;
          <span id="${meterId}_tags">${cnt}</span>/30 tags fit
          ${str.length > 500 ? ' <span style="color:var(--rose)">⚠ exceeds limit</span>' : ''}
        </div>
      </div>
    `;

    // Toggle expand function
    const headEl = block.querySelector('.catblock-head');
    const toggleBtn = block.querySelector('.expand-toggle');
    const toggleExpand = (e) => {
      if (e) e.stopPropagation();
      const isExpanded = block.classList.toggle('expanded');
      headEl.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
      toggleBtn.textContent = isExpanded ? '▲' : '▼';
    };

    headEl.onclick = (e) => toggleExpand(e);
    headEl.onkeydown = (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        toggleExpand(e);
      }
    };

    // Copy button
    block.querySelector('#copy_' + areaId).onclick = (e) => {
      e.stopPropagation();
      safeCopy(document.getElementById(areaId).value, cat.label + ' tags copied');
    };

    // Live character + tag counter listener
    const ta = block.querySelector('#' + areaId);
    const charsEl = block.querySelector('#' + meterId + '_chars');
    const tagsEl  = block.querySelector('#' + meterId + '_tags');
    const warnEl  = block.querySelector('#' + meterId);
    ta.addEventListener('input', () => {
      const v = ta.value;
      const c = v.length;
      const t = v ? v.split(',').filter(s => s.trim()).length : 0;
      charsEl.textContent = c;
      tagsEl.textContent  = t;
      charsEl.style.color = c > 500 ? 'var(--rose)' : '';
      let warnSpan = warnEl.querySelector('span[data-warn]');
      if (c > 500) {
        if (!warnSpan) {
          warnSpan = document.createElement('span');
          warnSpan.setAttribute('data-warn', '1');
          warnSpan.style.color = 'var(--rose)';
          warnSpan.textContent = ' ⚠ exceeds limit';
          warnEl.appendChild(warnSpan);
        }
      } else if (warnSpan) {
        warnSpan.remove();
      }
    });

    masonryGrid.appendChild(block);
  });

  container.appendChild(masonryGrid);
}

function buildPlaylistTab(container){
  if(!container || !currentData || !currentData.playlistMetadata) return;
  container.innerHTML = '';
  const pl = currentData.playlistMetadata;

  const masonryGrid = document.createElement('div');
  masonryGrid.className = 'masonry-grid';

  // Helper to build a standard accordion card identical to YouTube Metadata
  function createAccordionCard(opts) {
    return createAccordionCardHelper({ ...opts, container: masonryGrid });
  }

  // 1️⃣ Playlist Name Options
  const nameRowsHtml = (pl.playlistNames || []).map(name => `
    <div class="titleopt" style="cursor:pointer;" data-name="${name.replace(/"/g, '&quot;')}">
      <span>${name}</span>
      <span class="len">${name.length} chars</span>
    </div>
  `).join('');

  const nameCard = createAccordionCard({
    id: 'plnames',
    title: '1️⃣ Playlist Name Options',
    copyButtons: [{
      btnId: 'copyPlNamesAll',
      label: 'copy first',
      action: () => safeCopy((pl.playlistNames && pl.playlistNames[0]) || '', 'Playlist name copied')
    }],
    contentHtml: `
      <div style="font-family:'Inter',sans-serif;font-size:12px;font-weight:600;color:var(--muted);margin-bottom:8px;">Click any title option to copy</div>
      <div>${nameRowsHtml}</div>
    `
  });
  nameCard.querySelectorAll('.titleopt').forEach(el => {
    el.onclick = (e) => {
      e.stopPropagation();
      safeCopy(el.getAttribute('data-name'), 'Playlist name copied');
    };
  });

  // 2️⃣ Playlist Description
  createAccordionCard({
    id: 'pldesc',
    title: '2️⃣ Playlist Description',
    copyButtons: [{
      btnId: 'copyPlDesc',
      label: 'copy',
      action: () => safeCopy(document.getElementById('plDescArea').value, 'Playlist description copied')
    }],
    contentHtml: `
      <textarea id="plDescArea" style="min-height:120px;">${pl.playlistDescription || ''}</textarea>
      <div class="charmeter" style="margin-top:6px;">${(pl.playlistDescription || '').length} characters</div>
    `
  });

  // 3️⃣ Call to Action (CTA)
  createAccordionCard({
    id: 'plcta',
    title: '3️⃣ Call to Action (CTA)',
    copyButtons: [{
      btnId: 'copyPlCta',
      label: 'copy',
      action: () => safeCopy(document.getElementById('plCtaArea').value, 'CTA copied')
    }],
    contentHtml: `
      <textarea id="plCtaArea" style="min-height:75px;">${pl.ctaText || ''}</textarea>
    `
  });

  // 4️⃣ Practical tags field (comma-separated, ≤500 chars)
  createAccordionCard({
    id: 'pltags',
    title: '🏷️ Practical Tags Field (≤500 chars)',
    copyButtons: [{
      btnId: 'copyPlTags',
      label: 'copy',
      action: () => safeCopy(document.getElementById('plTagsArea').value, 'Playlist keywords copied')
    }],
    contentHtml: `
      <textarea id="plTagsArea" style="min-height:75px;">${pl.practicalTagsField || ''}</textarea>
      <div class="charmeter" style="margin-top:6px;">${(pl.practicalTagsField || '').length}/500 chars · YouTube playlists have no native tags field — paste this in the description/title text.</div>
    `
  });

  // 5️⃣ High Reach SEO Hashtags
  if ((pl.highReachHashtags || []).length) {
    const hrHashes = pl.highReachHashtags;
    createAccordionCard({
      id: 'highreach',
      title: '4️⃣ High Reach SEO Hashtags',
      copyButtons: [{
        btnId: 'copyHighReach',
        label: 'copy all',
        action: () => safeCopy(hrHashes.join(' '), 'High-reach hashtags copied')
      }],
      contentHtml: `
        <div class="taglist" style="padding:0 0 6px;">
          ${hrHashes.map(h => `<span class="tag"><span class="taglabel">${h}</span></span>`).join('')}
        </div>
      `
    });
  }

  // 6️⃣ SEO Keywords / Tags List
  if ((pl.seoKeywordsList || []).length) {
    const kwList = pl.seoKeywordsList;
    createAccordionCard({
      id: 'seokwlist',
      title: '5️⃣ SEO Keywords / Tags Bank',
      copyButtons: [{
        btnId: 'copyPlSeoList',
        label: 'copy all',
        action: () => safeCopy(document.getElementById('plSeoListArea').value, 'SEO keyword list copied')
      }],
      contentHtml: `
        <textarea id="plSeoListArea" style="min-height:110px;">${kwList.join('\n')}</textarea>
        <div class="charmeter" style="margin-top:6px;">${kwList.length} keywords</div>
      `
    });
  }

  // 7️⃣ Thumbnail Concept
  createAccordionCard({
    id: 'plthumb',
    title: '6️⃣ Thumbnail Concept',
    copyButtons: [{
      btnId: 'copyPlThumb',
      label: 'copy',
      action: () => safeCopy(document.getElementById('plThumbArea').value, 'Thumbnail concept copied')
    }],
    contentHtml: `
      <textarea id="plThumbArea" style="min-height:120px;">${pl.thumbnailConcept || ''}</textarea>
    `
  });

  // 8️⃣ Thumbnail AI Image Prompt
  createAccordionCard({
    id: 'plthumbprompt',
    title: '7️⃣ Thumbnail AI Image Prompt',
    copyButtons: [{
      btnId: 'copyPlThumbPrompt',
      label: 'copy',
      action: () => safeCopy(document.getElementById('plThumbPromptArea').value, 'Thumbnail prompt copied')
    }],
    contentHtml: `
      <textarea id="plThumbPromptArea" style="min-height:85px;">${pl.thumbnailAiPrompt || ''}</textarea>
    `
  });

  // ── ALL PLAYLIST SEO CATEGORIES (filtered by activeCats) ──
  activeCategories().forEach(cat => {
    let hashes = cat.hashtags || [];
    let keywords = cat.keywords || [];

    if (cat.key === 'lowcomp' && pl.lowCompHashtags && pl.lowCompHashtags.length) {
      hashes = pl.lowCompHashtags;
    }
    if (cat.key === 'lowcomp' && pl.lowCompKeywords && pl.lowCompKeywords.length) {
      keywords = pl.lowCompKeywords;
    }
    if (cat.key === 'hidden' && pl.hiddenHashtags && pl.hiddenHashtags.length) {
      hashes = pl.hiddenHashtags;
    }
    if (cat.key === 'hidden' && pl.hiddenKeywords && pl.hiddenKeywords.length) {
      keywords = pl.hiddenKeywords;
    }
    if (cat.key === 'seo' && pl.seoKeywordsList && pl.seoKeywordsList.length) {
      keywords = pl.seoKeywordsList;
    }

    const hashTagsHtml = hashes.map(h => `<span class="tag"><span class="taglabel">${h}</span></span>`).join('');
    const kwText = keywords.join('\n');

    const copyButtons = [];
    if (hashes.length) {
      copyButtons.push({
        btnId: `copyHash_${cat.key}`,
        label: 'copy hashtags',
        action: () => safeCopy(hashes.join(' '), `${cat.label} hashtags copied`)
      });
    }
    if (keywords.length) {
      copyButtons.push({
        btnId: `copyKw_${cat.key}`,
        label: 'copy keywords',
        action: () => {
          const ta = document.getElementById(`plKwArea_${cat.key}`);
          safeCopy(ta ? ta.value : kwText, `${cat.label} keywords copied`);
        }
      });
    }

    createAccordionCard({
      id: `plcat_${cat.key}`,
      title: `${cat.label} SEO Set`,
      color: cat.color,
      copyButtons: copyButtons,
      contentHtml: `
        ${hashes.length ? `
          <div style="font-family:'Inter',sans-serif;font-size:12px;font-weight:600;color:var(--muted);margin-bottom:6px;">Hashtags (${hashes.length})</div>
          <div class="taglist" style="padding:0 0 10px;">${hashTagsHtml}</div>
        ` : ''}
        ${keywords.length ? `
          <div style="font-family:'Inter',sans-serif;font-size:12px;font-weight:600;color:var(--muted);margin:6px 0;">Keywords (${keywords.length})</div>
          <textarea id="plKwArea_${cat.key}" style="min-height:85px;">${kwText}</textarea>
        ` : ''}
      `
    });
  });

  container.appendChild(masonryGrid);
}

function renderUI(){
  if(!currentData) return;
  document.getElementById('results').style.display = 'block';
  document.getElementById('resultMeta').textContent = `-- ${currentData.totalCategories} categories × 30 = ${currentData.totalItems} items  ·  topic: ${currentData.videoTopic}`;

  renderListTab(document.getElementById('tab-hashtags'), true);
  renderListTab(document.getElementById('tab-keywords'), false);
  buildMetadataTab(document.getElementById('tab-metadata'));
  buildPlaylistTab(document.getElementById('tab-playlist'));
}

function safeCopy(text, successMsg){
  if(navigator.clipboard && navigator.clipboard.writeText){
    navigator.clipboard.writeText(text).then(
      ()=>showToast(successMsg),
      ()=>fallbackCopy(text, successMsg)
    );
  } else {
    fallbackCopy(text, successMsg);
  }
}

function fallbackCopy(text, successMsg){
  try{
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    const ok = document.execCommand('copy');
    document.body.removeChild(ta);
    showToast(ok ? successMsg : 'Copy blocked — select text manually');
  } catch(e){
    showToast('Copy blocked — select text manually');
  }
}

function showToast(msg){
  const t = document.getElementById('toast');
  if(!t) return;
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(()=>t.classList.remove('show'), 1400);
}

function switchToPage(pageKey){
  document.querySelectorAll('.pagebtn').forEach(btn=>{
    btn.classList.toggle('active', btn.dataset.page === pageKey);
  });
  document.querySelectorAll('.pagepanel').forEach(p=>p.style.display='none');
  const target = document.getElementById('page-' + pageKey);
  if(target) target.style.display = 'block';
}

// Page switcher buttons
document.querySelectorAll('.pagebtn').forEach(btn=>{
  btn.onclick = ()=>{
    switchToPage(btn.dataset.page);
  };
});


// Theme Toggle
const themeToggle = document.getElementById('themeToggle');
if(themeToggle) {
  themeToggle.addEventListener('click', ()=>{
    const html = document.documentElement;
    const isDark = html.getAttribute('data-theme') === 'dark';
    if(isDark){
      html.removeAttribute('data-theme');
      themeToggle.textContent = '☾ Dark';
    } else {
      html.setAttribute('data-theme', 'dark');
      themeToggle.textContent = '☀ Light';
    }
  });
}

function scheduleAutoGenerate(){
  clearTimeout(window.__autoGenTimer);
  window.__autoGenTimer = setTimeout(fetchStudioData, 150);
}

document.addEventListener('DOMContentLoaded', ()=>{
  renderCatDropdown();
  const channelInp = document.getElementById('channelInput');
  const topicInp = document.getElementById('topicInput');
  const runBtn = document.getElementById('runBtn');

  if(channelInp) {
    channelInp.addEventListener('input', scheduleAutoGenerate);
    channelInp.addEventListener('keydown', (e) => { if (e.key === 'Enter') fetchStudioData(); });
  }
  if(topicInp) {
    topicInp.addEventListener('input', scheduleAutoGenerate);
    topicInp.addEventListener('keydown', (e) => { if (e.key === 'Enter') fetchStudioData(); });
  }
  if(runBtn) runBtn.addEventListener('click', fetchStudioData);

  // Prompt Interface bindings
  const promptArea = document.getElementById('promptArea');
  const promptCharCount = document.getElementById('promptCharCount');
  const copyPromptBtn = document.getElementById('copyPromptBtn');
  const clearPromptBtn = document.getElementById('clearPromptBtn');
  const generatePromptBtn = document.getElementById('generatePromptBtn');

  if(promptArea) {
    promptArea.addEventListener('input', () => {
      if(promptCharCount) promptCharCount.textContent = promptArea.value.length;
    });
    promptArea.addEventListener('keydown', async (e) => {
      if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
        e.preventDefault();
        await generatePromptResult();
      }
    });
  }
  if(generatePromptBtn) {
    generatePromptBtn.addEventListener('click', async (e) => {
      e.stopPropagation();
      await generatePromptResult();
    });
  }
  if(copyPromptBtn && promptArea) {
    copyPromptBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      safeCopy(promptArea.value, 'Prompt copied');
    });
  }
  if(clearPromptBtn && promptArea) {
    clearPromptBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      promptArea.value = '';
      if(promptCharCount) promptCharCount.textContent = '0';
      const promptResults = document.getElementById('promptResults');
      if(promptResults) promptResults.innerHTML = '';
      showToast('Prompt cleared');
    });
  }

  // Studio AI Chatbot
  const chatBubble = document.getElementById('chatBubble');
  const chatWindow = document.getElementById('chatWindow');
  const chatClose = document.getElementById('chatClose');
  const chatInput = document.getElementById('chatInput');
  const chatSend = document.getElementById('chatSend');
  const chatMsgs = document.getElementById('chatMsgs');

  let chatHistory = [];

  function openStudioAi(initialText = '') {
    if(!chatWindow) return;
    chatWindow.classList.add('open');
    if(chatInput) {
      if(initialText) chatInput.value = initialText;
      chatInput.focus();
    }
  }

  function closeStudioAi() {
    if(chatWindow) chatWindow.classList.remove('open');
  }

  function appendChatMsg(role, content, isHtml = false, isError = false, isLoading = false) {
    if(!chatMsgs) return null;
    const msg = document.createElement('div');
    msg.className = `chatmsg ${role}${isError ? ' err' : ''}${isLoading ? ' loading' : ''}`;
    if(isHtml) {
      msg.innerHTML = content;
    } else {
      msg.textContent = content;
    }
    chatMsgs.appendChild(msg);
    chatMsgs.scrollTop = chatMsgs.scrollHeight;
    return msg;
  }

  let isStudioAiSending = false;

  async function sendStudioAiMessage() {
    if(isStudioAiSending) return;
    if(!chatInput || !chatSend) return;
    const text = chatInput.value.trim();
    if(!text) return;

    isStudioAiSending = true;

    // Append user message
    appendChatMsg('user', text);
    chatHistory.push({ role: 'user', content: text });
    chatInput.value = '';

    // Show loading state
    chatInput.disabled = true;
    chatSend.disabled = true;
    const loadingEl = appendChatMsg('bot', 'Studio AI is thinking...', false, false, true);

    try {
      const topic = cleanTopicString(currentTopic());
      const channel = currentChannelName();
      const selectedCategories = Array.from(activeCats);
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ channel, topic, selectedCategories, messages: chatHistory })
      });

      if(loadingEl && loadingEl.parentNode) {
        loadingEl.parentNode.removeChild(loadingEl);
      }

      if(!res.ok) throw new Error('Network error');
      const data = await res.json();

      if(data.error) {
        appendChatMsg('bot', data.error, false, true);
      } else if(data.reply) {
        const botMsgDiv = document.createElement('div');
        botMsgDiv.className = 'chatmsg bot';
        botMsgDiv.innerHTML = `<div class="chatmsg-head" style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;"><strong>Studio AI</strong><button class="copybtn chat-copy-btn">copy</button></div><div>${data.reply.replace(/\n/g, '<br>')}</div>`;
        const copyBtn = botMsgDiv.querySelector('.chat-copy-btn');
        if(copyBtn) {
          copyBtn.onclick = (e) => {
            e.stopPropagation();
            safeCopy(formatYouTubeDescription(data.reply), 'Description copied');
          };
        }
        chatMsgs.appendChild(botMsgDiv);
        chatMsgs.scrollTop = chatMsgs.scrollHeight;
        chatHistory.push({ role: 'assistant', content: data.reply });
      }
    } catch(err) {
      if(loadingEl && loadingEl.parentNode) {
        loadingEl.parentNode.removeChild(loadingEl);
      }
      appendChatMsg('bot', 'Studio AI could not connect. Please try again.', false, true);
    } finally {
      isStudioAiSending = false;
      chatInput.disabled = false;
      chatSend.disabled = false;
      chatInput.focus();
    }
  }

  if(chatBubble) {
    chatBubble.addEventListener('click', (e) => {
      e.stopPropagation();
      if(chatWindow && chatWindow.classList.contains('open')) {
        closeStudioAi();
      } else {
        openStudioAi();
      }
    });
  }

  if(chatClose) {
    chatClose.addEventListener('click', (e) => {
      e.stopPropagation();
      closeStudioAi();
    });
  }

  if(chatSend) {
    chatSend.addEventListener('click', (e) => {
      e.stopPropagation();
      sendStudioAiMessage();
    });
  }

  if(chatInput) {
    chatInput.addEventListener('keydown', (e) => {
      if(e.key === 'Enter') {
        e.preventDefault();
        sendStudioAiMessage();
      }
    });
  }

  // Auto fetch initial data from Spring Boot API on load
  fetchStudioData();
});
