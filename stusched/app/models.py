from datetime import date
from django.db import models
from django.contrib.auth.models import User


class Course(models.Model):
    name = models.CharField(max_length=100)
    url = models.URLField(blank=True)
    active = models.BooleanField(default=True)

    def __str__(self):
        return self.name.__str__()

STATUSES = ((1, 'Proposed'), (2, 'Accepting'), (3, 'Scheduled'))


class Section(models.Model):
    start_time = models.DateTimeField()
    hours_per_day = models.DecimalField(max_digits=4, decimal_places=2)
    num_days = models.IntegerField(default=1)
    course = models.ForeignKey(Course)
    price = models.IntegerField(null=True, blank=True)
    min_students = models.IntegerField(default=3)
    max_students = models.IntegerField(default=6)
    scheduled_status = models.IntegerField(choices=STATUSES)
    notes = models.TextField(blank=True)
    private_notes = models.TextField(blank=True)

    def __str__(self):
        return '%s %s' % (self.start_time, self.course.name)

    def enrolled(self):
        return len(self.student_set.all())


class Parent(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField(null=True, blank=True)
    notes = models.TextField(blank=True)
    code  = models.CharField(max_length=100, null=True, blank=True)
    # Set via: update app_parent set code = md5(random()::text) where code is null;
    users = models.ManyToManyField(User, blank=True)

    def __str__(self):
        return self.name.__str__()


class Student(models.Model):
    name            = models.CharField(max_length=100)
    active          = models.BooleanField(default=True)
    birthdate       = models.DateField(null=True, blank=True)
    parent          = models.ForeignKey(Parent)
    email           = models.EmailField(null=True, blank=True)
    aptitude        = models.IntegerField(null=True, blank=True)
    sections        = models.ManyToManyField(Section, blank=True)
    wants_courses   = models.ManyToManyField(Course, blank=True)
    when_available  = models.TextField(blank=True)
    notes           = models.TextField(blank=True)
    private_notes   = models.TextField(blank=True)

    def age(self):
        if not self.birthdate: return ''
        today = date.today()
        return "%.2f" % ((today - self.birthdate).days / 365.24)

    def sections_taken(self):
        return ', '.join([section.course.name for section in self.sections.all()])

    def courses_wanted(self):
        return ', '.join([course.name for course in self.wants_courses.all()])

    @property
    def sections_by_time(self):
        return self.sections.all().order_by('start_time')

    def __str__(self):
        return self.name.__str__()


class KnowledgeItem(models.Model):
    name        = models.CharField(max_length=100)
    students    = models.ManyToManyField(Student, through='Knows')

    def __str__(self):
        return self.name.__str__()


class Knows(models.Model):
    student     = models.ForeignKey(Student, on_delete=models.CASCADE)
    item        = models.ForeignKey(KnowledgeItem, on_delete=models.CASCADE)
    quantity    = models.IntegerField()

    def __str__(self):
        return '%s %s %d' % (self.student.name, self.item.name, self.quantity)

