// Small progressive enhancements. Everything works without JavaScript.
(function () {
    'use strict';

    // Ask before destructive actions: <form data-confirm="Are you sure?">
    document.addEventListener('submit', function (event) {
        var message = event.target.getAttribute('data-confirm');
        if (message && !window.confirm(message)) {
            event.preventDefault();
        }
    });

    // Department-wise assignment: show only agents of the chosen department.
    // <select data-department-select> controls the sibling <select data-agent-select>
    // whose options carry data-department="{id}".
    function filterAgents(departmentSelect) {
        var form = departmentSelect.closest('form');
        var agentSelect = form && form.querySelector('[data-agent-select]');
        if (!agentSelect) return;
        var departmentId = departmentSelect.value;
        var available = 0;
        Array.prototype.forEach.call(agentSelect.options, function (option) {
            var dept = option.getAttribute('data-department');
            if (dept === null) return; // the "auto" option is always available
            var visible = dept === departmentId;
            option.hidden = !visible;
            option.disabled = !visible;
            if (visible) available++;
        });
        var selected = agentSelect.options[agentSelect.selectedIndex];
        if (selected && selected.disabled) agentSelect.value = '';
        var autoOption = agentSelect.querySelector('option[value=""]');
        if (autoOption) {
            autoOption.textContent = available > 0
                ? 'Auto – least loaded agent (' + available + ' available)'
                : 'No active agents in this department';
        }
    }

    document.querySelectorAll('[data-department-select]').forEach(function (select) {
        filterAgents(select);
        select.addEventListener('change', function () { filterAgents(select); });
    });

    // Keep the newest chat message in view.
    var conversation = document.querySelector('.conversation');
    if (conversation) conversation.scrollTop = conversation.scrollHeight;
})();
