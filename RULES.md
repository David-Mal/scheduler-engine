# Scheduling Rules

## Hard constraints (must never be violated)

1. **One activity per person per service.** A person cannot be scheduled for two different activities in the same service.
   Example: in a Sunday morning service, the same person cannot be scheduled for both prayer and the sermon.
2. **Eligibility per activity.** Only a limited set of people are allowed to be scheduled for each activity (sermon, prayer, singing), and only those people can be scheduled for it.
3. **Monthly limits per person.** Some people can be scheduled at most 1 or 2 times per month for the sermon or prayer, while others have no such limit.
4. **No double sermon on the same day.** On Sundays, the same person cannot give the sermon in both the morning and the evening service.

## Soft constraints (preferences, penalized in the score)

5. **Rotation.** If a person was scheduled for the sermon or prayer last week, they should not get the same activity this week, so that everyone gets a chance to participate.
6. **Fair singing distribution.** The number of times people sing in a month should be balanced. There are multiple groups, and they share the singing duties.

## Calendar generation

7. **Service days.** The dates of the services are generated automatically for the selected month (default: the current month). The user selects the days of the week that have services (Sunday by default, other days optional), and the system generates all matching dates.
8. **Christian holidays.** In months with Christian holidays, the system adds their dates automatically. The user selects which holidays have services, and those dates are added to the schedule.

## Availability and replanning

9. **Availability confirmation.** Before a schedule is published, each person confirms their availability. If a person is unavailable for a slot, only that slot is regenerated; the rest of the schedule stays unchanged.
10. **No empty slots.** A slot is never left empty. If a person is unavailable, the system first looks for a replacement that respects all rules. If none exists, it relaxes the rotation rule (5): the person who had the same activity in the previous calendar week or the previous service may be repeated. If that is still not enough, it swaps assignments with a person scheduled in another slot, as long as both slots still satisfy the hard constraints (1-4).
## Activities and singing

11. **Activities.** The only activities are prayer, sermon and singing.
12. **Singing groups.** Singing is assigned to groups (choir, orchestra, youth group, etc.), not to individual people. Singing duties are balanced across groups over the month (rule 6).