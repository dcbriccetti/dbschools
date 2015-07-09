from django.db import models

class Course(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField()

    def __str__(self):
        return self.name.__str__()

class Section(models.Model):
    start_time = models.DateTimeField()
    duration_per_day = models.DurationField()
    num_days = models.DecimalField(max_digits=3, decimal_places=0, default=1)
    course = models.ForeignKey(Course)
    price = models.DecimalField(max_digits=6, decimal_places=2)
    min_students = models.DecimalField(max_digits=3, decimal_places=0, default=3)
    max_students = models.DecimalField(max_digits=3, decimal_places=0, default=6)

    def end_time(self): return self.start_time + self.duration_per_day

    def __str__(self):
        return "At %s" % (self.start_time.__str__())

class Parent(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()

    def __str__(self):
        return self.name.__str__()

class Student(models.Model):
    name = models.CharField(max_length=100)
    parent = models.ForeignKey(Parent)
    sections = models.ManyToManyField(Section, blank=True)

    def __str__(self):
        return self.name.__str__()
