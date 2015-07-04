from django.db import models

class Course(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField()

    def __str__(self):
        return self.name.__str__()

class Section(models.Model):
    start_date = models.DateTimeField()
    course = models.ForeignKey(Course)
    price = models.DecimalField(max_digits=6, decimal_places=2)

    def __str__(self):
        return "%s on %s" % (self.course.name, self.start_date.__str__())

class Parent(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()

    def __str__(self):
        return self.name.__str__()

class Student(models.Model):
    name = models.CharField(max_length=100)
    parents = models.ManyToManyField(Parent)
    sections = models.ManyToManyField(Section, blank=True)

    def __str__(self):
        return self.name.__str__()
