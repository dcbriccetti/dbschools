from django.db import models


class Course(models.Model):
    name = models.CharField(max_length=100)
    url = models.URLField(blank=True)
    active = models.BooleanField(default=True)

    def __str__(self):
        return self.name.__str__()

PROPOSED = 1
ACCEPTING = 2
SCHEDULED = 3

statuses = {
    PROPOSED:   'proposed',
    ACCEPTING:  'accepting',
    SCHEDULED:  'scheduled'
}


class Section(models.Model):
    start_time = models.DateTimeField()
    duration_per_day = models.DurationField()
    num_days = models.DecimalField(max_digits=3, decimal_places=0, default=1)
    course = models.ForeignKey(Course)
    price = models.DecimalField(max_digits=6, decimal_places=2)
    min_students = models.IntegerField(default=3)
    max_students = models.IntegerField(default=6)
    scheduled_status = models.IntegerField()
    notes = models.TextField(blank=True)

    def end_time(self): return self.start_time + self.duration_per_day

    def __str__(self):
        return '%s %s' % (self.start_time, self.course.name)

    def enrolled(self):
        return len(self.student_set.all())


class Parent(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()
    notes = models.TextField(blank=True)

    def __str__(self):
        return self.name.__str__()


class Student(models.Model):
    name = models.CharField(max_length=100)
    active = models.BooleanField(default=True)
    parent = models.ForeignKey(Parent)
    sections = models.ManyToManyField(Section, blank=True)
    notes = models.TextField(blank=True)

    def proposed_sections(self):
        return self.sections.filter(scheduled_status=PROPOSED)

    def accepting_sections(self):
        return self.sections.filter(scheduled_status=ACCEPTING)

    def enrolled_sections(self):
        return self.sections.filter(scheduled_status=SCHEDULED)

    def list_sections(self):
        return ', '.join([section.course.name for section in self.sections.all()])

    def __str__(self):
        return self.name.__str__()
