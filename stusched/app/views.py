from django.shortcuts import render
from .models import Course, Section

class ScheduledCourse(object):
    def __init__(self, name, description, sections):
        self.name = name
        self.description = description
        self.sections = sections

    def __str__(self, *args, **kwargs):
        return self.name + ' ' + self.description


def index(request):
    sections = Section.objects.order_by('start_time')
    scheduledCourses = set((s.course for s in sections))
    courses = (ScheduledCourse(c.name, c.description_with_br(),
        [s for s in sections if s.course == c]) for c in Course.objects.order_by('name') if c in scheduledCourses)

    return render(request, 'app/index.html', {'courses': courses})
