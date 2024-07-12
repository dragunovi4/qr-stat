WITH
initial AS (
	SELECT key as profile, value::integer FROM sys_extras WHERE key IN ('trial', 'single', 'evotor', 'franch', 'daught')
),
current AS (
	select (regexp_matches(coalesce(p.name, '2.77-daught.test'), '.\w*-(\w*)[\.]?'))[1] as profile, count(*) as value from customer c left join profile p on c.profile_id=p.id where c.name not like '%test%' and c.state = 'BOUND' group by (regexp_matches(coalesce(p.name, '2.77-daught.test'), '.\w*-(\w*)[\.]?'))[1]
),
trial AS (
	select (regexp_matches(coalesce(p.name, '2.77-daught.test'), '.\w*-(\w*)[\.]?'))[1] as profile, count(*) as value from customer c left join extras ex on ex.owner_id=c.id left join profile p on c.profile_id=p.id where c.name not like '%test%' and c.state = 'BOUND' and ex.key = 'tariff' and ex.value = 'trial' group by (regexp_matches(coalesce(p.name, '2.77-daught.test'), '.\w*-(\w*)[\.]?'))[1]
)
SELECT i.profile, c.value - coalesce(t.value, 0) - i.value as diff, round((c.value - coalesce(t.value, 0) - i.value)*100.0/i.value, 2) as percent, t.value as trial, c.value as current
FROM initial i
	left join current c on c.profile = i.profile
	left join trial t on t.profile = i.profile